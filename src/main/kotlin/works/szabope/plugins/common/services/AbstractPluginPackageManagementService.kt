package works.szabope.plugins.common.services

import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.remote.RemoteSdkProperties
import com.jetbrains.python.getOrThrow
import com.jetbrains.python.isSuccess
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.packaging.common.PythonRepositoryPackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.management.getInstalledPackageSnapshot
import com.jetbrains.python.packaging.management.toInstallRequest
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.CommonBundle

abstract class AbstractPluginPackageManagementService {

    protected abstract val project: Project

    abstract fun getRequirement(): PyRequirement

    fun canInstall(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return !PythonSdkUtil.isRemote(sdk) && checkInstalledRequirement().isFailure
    }

    fun isLocalEnvironment(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
    }

    open suspend fun reloadPackages(): Result<List<PythonPackage>>? {
        return try {
            getPackageManager()?.reloadPackages()?.getOrThrow()?.let { Result.success(it) }
        } catch (e: Exception) {
            // e.g. org.apache.hc.client5.http.HttpHostConnectException thrown when docker (in given SDK) is unavailable
            Result.failure(e)
        }
    }

    fun isWSL() = (project.pythonSdk?.sdkAdditionalData as? RemoteSdkProperties)?.sdkId?.startsWith("WSL") ?: false

    // open for testing purposes
    open fun checkInstalledRequirement(): Result<Unit> {
        val requirement = getRequirement()
        val packageManager =
            getPackageManager() ?: return Result.failure(UnsupportedOperationException("No package manager found"))
        val installedPackage = packageManager.getInstalledPackageSnapshot(requirement.name) ?: return Result.failure(
            PluginPackageManagementException.PackageNotInstalledException()
        )
        if (!getRequirement().match(PyPackage(installedPackage.name, installedPackage.version))) {
            return Result.failure(PluginPackageManagementException.PackageVersionObsoleteException())
        }
        return Result.success(Unit)
    }

    // open for testing purposes
    open suspend fun installRequirement(): Result<Unit> {
        val packageManager = getPackageManager()!!
        val requirement = getRequirement()
        val versionSpec = requirement.versionSpecs.firstOrNull()
        val specification = PythonRepositoryPackageSpecification(
            packageManager.repositoryManager.repositories.first(), requirement.name, versionSpec
        )
        val installResult = withBackgroundProgress(
            project, CommonBundle.message("configurable.installation_in_progress", requirement.name), cancellable = true
        ) {
            packageManager.installPackage(specification.toInstallRequest(), options = emptyList())
        }
        return if (installResult.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(PluginPackageManagementException.InstallationFailedException(installResult.errorOrNull!!.message))
        }
    }

    private fun getPackageManager(): PythonPackageManager? {
        return project.pythonSdk?.let { PythonPackageManager.forSdk(project, it) }
    }
}

sealed class PluginPackageManagementException : RuntimeException() {
    class InstallationFailedException(override val message: String) : PluginPackageManagementException()
    class PackageNotInstalledException : PluginPackageManagementException()
    class PackageVersionObsoleteException : PluginPackageManagementException()
}