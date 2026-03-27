package works.szabope.plugins.common.validator

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyPackage
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.PluginPackageManagementException
import java.io.File

abstract class AbstractToolValidator(protected val project: Project, private val messages: ToolValidatorMessages) {
    protected abstract val versionFlag: String
    protected abstract val packageName: String
    protected abstract fun getPackageManagementService(): AbstractPluginPackageManagementService

    fun validateExecutablePath(path: String?): String? {
        val p = path ?: return null
        require(p.isNotBlank())
        val file = File(p)
        if (!file.exists()) return messages.pathNotExists
        if (file.isDirectory) return messages.pathIsDirectory
        if (!file.canExecute()) return messages.pathNotExecutable
        return null
    }

    fun validateVersion(path: String): String? {
        val version = getVersionForExecutable(path) ?: return messages.unknownVersion
        if (!getPackageManagementService().getRequirement().match(PyPackage(packageName, version))) {
            return messages.invalidVersion
        }
        return null
    }

    suspend fun validateProjectSdk(): String? {
        getPackageManagementService().checkInstalledRequirement().onFailure {
            when (it) {
                is PluginPackageManagementException.PackageNotInstalledException -> return messages.notInstalled
                is PluginPackageManagementException.PackageVersionObsoleteException -> return messages.invalidVersion
            }
        }
        return null
    }

    protected fun getVersionForExecutable(pathToExecutable: String): String? {
        val commandLine = GeneralCommandLine(pathToExecutable, versionFlag)
        return runCatching {
            val processHandler = CapturingProcessHandler(commandLine)
            val processOutput = processHandler.runProcess(5000, true).stdout
            "(\\d+\\.\\d+\\.\\d+)".toRegex().find(processOutput)?.groups?.last()?.value
        }.getOrNull()
    }
}
