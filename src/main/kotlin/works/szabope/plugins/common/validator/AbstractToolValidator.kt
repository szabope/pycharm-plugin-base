package works.szabope.plugins.common.validator

import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.target.TargetedCommandLineBuilder
import com.intellij.execution.target.local.LocalTargetEnvironment
import com.intellij.execution.target.local.LocalTargetEnvironmentRequest
import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyPackage
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.PluginPackageManagementException
import java.io.File

abstract class AbstractToolValidator(protected val project: Project) {

    protected abstract val versionFlag: String
    protected abstract val packageName: String
    protected abstract fun getPackageManagementService(): AbstractPluginPackageManagementService

    protected abstract fun pathNotExistsMessage(): String
    protected abstract fun pathIsDirectoryMessage(): String
    protected abstract fun pathNotExecutableMessage(): String
    protected abstract fun unknownVersionMessage(): String
    protected abstract fun invalidVersionMessage(): String
    protected abstract fun notInstalledMessage(): String

    fun validateExecutablePath(path: String?): String? {
        val p = path ?: return null
        require(p.isNotBlank())
        val file = File(p)
        if (!file.exists()) return pathNotExistsMessage()
        if (file.isDirectory) return pathIsDirectoryMessage()
        if (!file.canExecute()) return pathNotExecutableMessage()
        return null
    }

    fun validateVersion(path: String): String? {
        val version = getVersionForExecutable(path) ?: return unknownVersionMessage()
        if (!getPackageManagementService().getRequirement().match(PyPackage(packageName, version))) {
            return invalidVersionMessage()
        }
        return null
    }

    suspend fun validateProjectSdk(): String? {
        getPackageManagementService().checkInstalledRequirement().onFailure {
            when (it) {
                is PluginPackageManagementException.PackageNotInstalledException -> return notInstalledMessage()
                is PluginPackageManagementException.PackageVersionObsoleteException -> return invalidVersionMessage()
            }
        }
        return null
    }

    protected fun getVersionForExecutable(pathToExecutable: String): String? {
        val targetEnvRequest = LocalTargetEnvironmentRequest()
        val targetEnvironment = LocalTargetEnvironment(LocalTargetEnvironmentRequest())

        @Suppress("UnstableApiUsage") val commandLineBuilder = TargetedCommandLineBuilder(targetEnvRequest)
        commandLineBuilder.setExePath(pathToExecutable)
        commandLineBuilder.addParameters(versionFlag)

        val targetCMD = commandLineBuilder.build()
        val process = targetEnvironment.createProcess(targetCMD)

        return runCatching {
            @Suppress("UnstableApiUsage") val processHandler = CapturingProcessHandler(
                process, targetCMD.charset, targetCMD.getCommandPresentation(targetEnvironment)
            )
            val processOutput = processHandler.runProcess(5000, true).stdout
            "(\\d+.\\d+.\\d+)".toRegex().find(processOutput)?.groups?.last()?.value
        }.getOrNull()
    }
}