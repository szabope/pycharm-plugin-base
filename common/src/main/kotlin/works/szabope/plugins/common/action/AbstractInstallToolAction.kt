package works.szabope.plugins.common.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.jetbrains.python.packaging.PyExecutionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.services.PluginPackageManagementService

class InstallationToolActionConfig(val messageInstalling: String, val messageInstalled: String)

abstract class AbstractInstallToolAction(private val config: InstallationToolActionConfig) : DumbAwareAction() {

    abstract fun getPackageManager(project: Project): PluginPackageManagementService

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        runWithModalProgressBlocking(project, config.messageInstalling) {
            withContext(Dispatchers.EDT) {
                getPackageManager(project).installRequirement()
                    .onSuccess {
                        notifyPanel(project, config.messageInstalled)
                        migrateSettings(project)
                    }
                    .onFailure { failure ->
                        when (failure) {
                            is PyExecutionException -> {
                                // At this point a notification has already been shown:
                                // PythonPackageManager.installPackageInternal -> util.runPackagingOperationOrShowErrorDialog
                                // This branch will be used to show notification as soon as it is removed from PythonPackageManager
                                // Current use is enabling testability
                                IDialogManager.showPyPackageInstallationErrorDialog(failure)
                            }

                            is PluginPackageManagementService.PackageNotFoundException -> {
                                if (failure.message != null) {
                                    IDialogManager.showPackageNotFoundDialog(failure.message!!)
                                } else {
                                    IDialogManager.showGeneralErrorDialog(failure)
                                }
                            }

                            else -> {
                                thisLogger().error(failure)
                                IDialogManager.showGeneralErrorDialog(failure)
                            }
                        }
                    }
            }
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { getPackageManager(it).canInstall() } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    abstract suspend fun notifyPanel(project: Project, message: String)
    abstract suspend fun migrateSettings(project: Project)
}