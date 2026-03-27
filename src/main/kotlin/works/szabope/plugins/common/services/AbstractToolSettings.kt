package works.szabope.plugins.common.services

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.jetbrains.python.sdk.pythonSdk
import org.jetbrains.annotations.TestOnly

abstract class AbstractToolSettings<S : BaseState>(
    val project: Project, defaultState: S
) : SimplePersistentStateComponent<S>(defaultState), Settings {

    private var initialized = false

    protected abstract fun getPackageManagementService(): AbstractPluginPackageManagementService
    protected abstract fun toolNotSetMessage(): String
    protected abstract fun isExecutableStateNull(): Boolean
    protected abstract fun isConfigFileStateNull(): Boolean
    protected abstract fun isArgumentsStateNull(): Boolean
    protected abstract fun initialState(): S

    override suspend fun initSettings(oldSettings: BasicSettingsData) {
        if (isExecutableStateNull()) {
            oldSettings.executablePath?.let { executablePath = it }
        }
        if (executablePath.isNotBlank() && project.pythonSdk == null) {
            useProjectSdk = false
        }
        if (isConfigFileStateNull()) {
            oldSettings.configFilePath?.let { configFilePath = it }
        }
        if (isArgumentsStateNull()) {
            oldSettings.arguments?.let { arguments = it }
        }
        if (workingDirectory == null) {
            workingDirectory = project.guessProjectDir()?.canonicalPath
        }
        initialized = true
    }

    override suspend fun getValidConfiguration(): Result<ToolExecutorConfiguration> {
        val workingDirectory = workingDirectory
        if (workingDirectory.isNullOrBlank()) {
            return Result.failure(ToolSettingsInvalidException("Working directory is required"))
        }
        if (!isToolSet()) {
            return Result.failure(ToolSettingsInvalidException(toolNotSetMessage()))
        }
        return Result.success(
            ToolExecutorConfiguration(
                executablePath,
                useProjectSdk,
                configFilePath,
                arguments,
                workingDirectory,
                excludeNonProjectFiles,
                scanBeforeCheckIn
            )
        )
    }

    private suspend fun isToolSet(): Boolean {
        return if (useProjectSdk) {
            project.pythonSdk != null && getPackageManagementService().checkInstalledRequirement().isSuccess
        } else {
            executablePath.isNotBlank()
        }
    }

    override fun isToolApplicable(): Boolean {
        if (workingDirectory.isNullOrBlank()) return false
        return if (useProjectSdk) project.pythonSdk != null else executablePath.isNotBlank()
    }

    fun isInitialized() = initialized

    @TestOnly
    fun reset() {
        loadState(initialState())
    }
}
