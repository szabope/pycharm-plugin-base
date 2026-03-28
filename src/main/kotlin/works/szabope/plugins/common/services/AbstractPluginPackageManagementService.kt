package works.szabope.plugins.common.services

import com.intellij.execution.ExecutionException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.packaging.PyPackageManagerUI
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk
import java.util.concurrent.Callable

abstract class AbstractPluginPackageManagementService {

    protected abstract val project: Project

    abstract fun getRequirement(): PyRequirement

    @Volatile private var packageInstalled: Boolean? = null

    fun canInstallSync(): Boolean {
        val sdk = project.pythonSdk ?: return false
        if (PythonSdkUtil.isRemote(sdk)) return false
        return packageInstalled != true
    }

    suspend fun canInstall(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return !PythonSdkUtil.isRemote(sdk) && checkInstalledRequirement().isFailure
    }

    fun isLocalEnvironment(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return ApplicationManager.getApplication().executeOnPooledThread(Callable {
            PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
        }).get()
    }

    fun isRemote(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return PythonSdkUtil.isRemote(sdk)
    }

    // open for testing purposes
    open suspend fun checkInstalledRequirement(): Result<Unit> {
        if (isRemote()) {
            packageInstalled = null
            return Result.failure(PluginPackageManagementException.SdkNotSupportedException())
        }
        val requirement = getRequirement()
        val packageManager =
            getPackageManager() ?: return Result.failure(UnsupportedOperationException("No package manager found"))
        @Suppress("UnstableApiUsage") val installedPackage =
            packageManager.listInstalledPackages().firstOrNull { it.name == requirement.name } ?: run {
                packageInstalled = false
                return Result.failure(PluginPackageManagementException.PackageNotInstalledException())
            }
        if (!getRequirement().match(PyPackage(installedPackage.name, installedPackage.version))) {
            packageInstalled = false
            return Result.failure(PluginPackageManagementException.PackageVersionObsoleteException())
        }
        packageInstalled = true
        return Result.success(Unit)
    }

    // open for testing purposes
    open suspend fun installRequirementWithCallback(callback: () -> Unit): Result<Unit> {
        packageInstalled = null
        val packageManager = getPackageManagerUI(callback)
            ?: return Result.failure(PluginPackageManagementException.InstallationFailedException("No package manager found"))
        val requirement = getRequirement()
        packageManager.install(listOf(requirement), emptyList<String>())
        // may still be a failure, but that is handled via PythonPackageManagerUI.sink internally
        return Result.success(Unit)
    }

    @Suppress("UnstableApiUsage")
    private fun getPackageManager(): PythonPackageManager? {
        return project.pythonSdk?.let { PythonPackageManager.forSdk(project, it) }
    }

    private fun getPackageManagerUI(callback: () -> Unit): PyPackageManagerUI? {
        val l = object : PyPackageManagerUI.Listener {
            override fun started() = Unit

            override fun finished(exceptions: List<ExecutionException?>?) {
                callback()
            }
        }
        return project.pythonSdk?.let { PyPackageManagerUI(project, it, l) }
    }
}

sealed class PluginPackageManagementException : RuntimeException() {
    class InstallationFailedException(override val message: String) : PluginPackageManagementException()
    class PackageNotInstalledException : PluginPackageManagementException()
    class PackageVersionObsoleteException : PluginPackageManagementException()
    class SdkNotSupportedException : PluginPackageManagementException()
}