package works.szabope.plugins.common.configurable

import androidx.annotation.VisibleForTesting
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.Condition
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.ui.components.JBRadioButton
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.ui.layout.and
import com.jetbrains.python.sdk.PySdkPopupFactory
import com.jetbrains.python.sdk.noInterpreterMarker
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.rd.util.Callable
import works.szabope.plugins.common.CommonBundle
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.trimToNull
import java.io.File
import javax.swing.JButton

data class ConfigurableConfiguration(
    val displayName: String,
    val helpTopic: String,
    val id: String,
    val installActionId: String,
    val installButtonText: String,
    val pickerTitle: String,
    val pickerDirectOptionTitle: String,
    val pickerDirectOptionFileFilter: GeneralConfigurable.FileFilter,
    val pickerDirectOptionEmptyWarning: String,
    val pickerDirectOptionVersionCheckProgressTitle: String,
    val pickerSdkOptionTitle: String,
    val configFilePickerRowComment: String,
    val recommendedArguments: String,
)

@Suppress("UnstableApiUsage")
abstract class GeneralConfigurable(
    private val project: Project, @param:VisibleForTesting val config: ConfigurableConfiguration
) : BoundSearchableConfigurable(config.displayName, config.helpTopic, config.id), Configurable.NoScroll {

    protected abstract val settings: Settings
    protected abstract val packageManager: AbstractPluginPackageManagementService
    protected abstract val defaultArguments: String

    abstract fun validateExecutable(path: String?): String?
    abstract fun validateSdk(builder: ValidationInfoBuilder, button: JBRadioButton): ValidationInfo?
    abstract fun validateConfigFilePath(
        builder: ValidationInfoBuilder, field: TextFieldWithBrowseButton
    ): ValidationInfo?

    private var executablePathError: String? = null
    private lateinit var pathToExecutableField: Cell<TextFieldWithBrowseButton>

    fun validateProjectDirectory(builder: ValidationInfoBuilder, field: TextFieldWithBrowseButton): ValidationInfo? {
        val path = field.text.trimToNull() ?: return null
        require(path.isNotBlank())
        val file = File(path)
        if (!file.exists()) {
            return builder.error(CommonBundle.message("configurable.path_to_project_directory.not_exist"))
        }
        if (!file.isDirectory) {
            return builder.error(CommonBundle.message("configurable.path_to_project_directory.is_not_directory"))
        }
        return null
    }

    override fun createPanel(): DialogPanel {
        val pnl = panel {
            indent {
                toolPicker()
                configFilePicker()
                argumentsField()
                projectDirectoryPicker()
                excludeNonProjectFilesCheckbox()
            }
        }
        pnl.registerValidators(disposable!!)
        pnl.validateAll()
        return pnl
    }

    override fun apply() {
        // apply is executed under write lock that we must not block by running an external process
        val futureExecutablePathValidity = ApplicationManager.getApplication().executeOnPooledThread(Callable {
            validateExecutable(pathToExecutableField.component.text)
        })
        executablePathError =
            runWithModalProgressBlocking(project, config.pickerDirectOptionVersionCheckProgressTitle) {
                futureExecutablePathValidity.get()
            }
        if ((createComponent() as DialogPanel).validateAll().isEmpty()) {
            super.apply()
        }
    }

    class FileFilter(private val fileNames: List<String>) : Condition<VirtualFile> {
        override fun value(t: VirtualFile?): Boolean {
            return fileNames.contains(t?.name ?: return false)
        }
    }

    private fun canInstall(): Boolean {
        val futureCanInstall = ApplicationManager.getApplication().executeOnPooledThread(Callable {
            packageManager.canInstall()
        })
        return runWithModalProgressBlocking(project, CommonBundle.message("configurable.progress.can_install")) {
            futureCanInstall.get()
        }
    }

    private fun isLocalEnvironment(): Boolean {
        val futureIsLocalEnvironment = ApplicationManager.getApplication().executeOnPooledThread(Callable {
            packageManager.isLocalEnvironment()
        })
        return runWithModalProgressBlocking(
            project, CommonBundle.message("configurable.progress.is_local_environment")
        ) {
            futureIsLocalEnvironment.get()
        }
    }

    private fun Row.installButton(enabled: ComponentPredicate) {
        val buttonClicked = AtomicBooleanProperty(false)
        val action = ActionManager.getInstance().getAction(config.installActionId)
        label(project.pythonSdk?.let { PySdkPopupFactory.shortenNameInPopup(it, 50) } ?: noInterpreterMarker).align(
            Align.FILL
        )
        lateinit var result: Cell<JButton>
        result = button(config.installButtonText) {
            val dataContext = DataManager.getInstance().getDataContext(result.component)
            val event = AnActionEvent.createEvent(
                action, dataContext, null, ActionPlaces.UNKNOWN, ActionUiKind.NONE, null
            )
            buttonClicked.set(true)
            ActionUtil.performAction(action, event)
        }.enabledIf(object : ComponentPredicate() {
            override fun invoke() = !buttonClicked.get() && canInstall()
            override fun addListener(listener: (Boolean) -> Unit) {
                buttonClicked.afterChange(listener)
            }
        }.and(enabled))
    }

    private fun Panel.toolPicker() = buttonsGroup(title = config.pickerTitle) {
        row {
            @Suppress("KotlinConstantConditions") val executableOption =
                radioButton(config.pickerDirectOptionTitle, !USE_PROJECT_SDK)
            executableOption.component
            val executableChooserDescriptor =
                FileChooserDescriptor(true, false, false, false, false, false).withFileFilter(
                    config.pickerDirectOptionFileFilter
                )
            pathToExecutableField = textFieldWithBrowseButton(
                project = project, fileChooserDescriptor = executableChooserDescriptor
            )
            pathToExecutableField.align(Align.FILL).bindText(
                getter = { settings.executablePath },
                setter = { settings.executablePath = it.trim() },
            ).validationOnInput { field ->
                executablePathError = null
                if (field.text.isBlank()) {
                    return@validationOnInput warning(config.pickerDirectOptionEmptyWarning)
                }
                null
            }.validationOnApply {
                return@validationOnApply executablePathError?.let { error(it) }
            }.resizableColumn().enabledIf(executableOption.selected)
        }.rowComment(CommonBundle.message("configurable.executable_path_option_marked_for_removal"))
            .layout(RowLayout.PARENT_GRID)
        row {
            val sdkOption = radioButton(config.pickerSdkOptionTitle, USE_PROJECT_SDK).enabled(
                project.pythonSdk != null
            ).validationOnInput(::validateSdk)
            sdkOption.component
            installButton(sdkOption.selected)
        }.rowComment(
            comment = if (!isLocalEnvironment()) {
                CommonBundle.message("configurable.system_wide_installation_warning")
            } else {
                ""
            }, maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
        ).layout(RowLayout.PARENT_GRID)
    }.bind(getter = { settings.useProjectSdk }, setter = { settings.useProjectSdk = it })

    private fun Panel.configFilePicker() = row {
        label(CommonBundle.message("configurable.config_file.label"))
        textFieldWithBrowseButton(project = project).align(Align.FILL).bindText(
            getter = { settings.configFilePath },
            setter = { settings.configFilePath = it.trim() },
        ).validationOnApply(::validateConfigFilePath)
    }.rowComment(
        config.configFilePickerRowComment, maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
    ).layout(RowLayout.PARENT_GRID)

    private fun Panel.argumentsField() = row {
        label(CommonBundle.message("configurable.arguments.label"))
        textField().align(Align.FILL).bindText(
            getter = { settings.arguments.ifBlank { defaultArguments } },
            setter = { settings.arguments = it.trim() },
        )
    }.rowComment(
        CommonBundle.message("configurable.arguments.hint_recommended", config.recommendedArguments),
        maxLineLength = MAX_LINE_LENGTH_WORD_WRAP
    ).layout(RowLayout.PARENT_GRID)

    private fun Panel.projectDirectoryPicker() = row {
        label(CommonBundle.message("configurable.project_directory.label"))
        val directoryChooserDescriptor = FileChooserDescriptor(false, true, false, false, false, false)
        textFieldWithBrowseButton(
            project = project, fileChooserDescriptor = directoryChooserDescriptor
        ).align(Align.FILL).bindText(
            getter = { settings.projectDirectory ?: project.guessProjectDir()?.path ?: "" },
            setter = { settings.projectDirectory = it },
        ).validationOnInput { field ->
            if (field.text.isBlank()) {
                return@validationOnInput warning(CommonBundle.message("configurable.project_directory.empty_warning"))
            }
            null
        }.validationOnApply(::validateProjectDirectory)
    }.layout(RowLayout.PARENT_GRID)

    private fun Panel.excludeNonProjectFilesCheckbox() = row {
        checkBox(CommonBundle.message("configurable.exclude_non_project_files.label")).bindSelected(
            getter = { settings.excludeNonProjectFiles },
            setter = { settings.excludeNonProjectFiles = it })
    }.layout(RowLayout.PARENT_GRID)

    companion object {
        const val USE_PROJECT_SDK = true
    }
}