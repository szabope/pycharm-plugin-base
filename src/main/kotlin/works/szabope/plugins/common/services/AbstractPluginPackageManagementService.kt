package works.szabope.plugins.common.services

import com.intellij.openapi.project.Project
import com.intellij.remote.RemoteSdkProperties
import com.jetbrains.python.getOrThrow
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.packaging.common.PythonRepositoryPackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.packaging.management.getInstalledPackageSnapshot
import com.jetbrains.python.packaging.management.toInstallRequest
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk

abstract class AbstractPluginPackageManagementService : PluginPackageManagementService {

    protected abstract val project: Project

    override fun canInstall(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return !PythonSdkUtil.isRemote(sdk) && checkInstalledRequirement().isFailure
    }

    override fun isLocalEnvironment(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
    }

    override suspend fun reloadPackages(): Result<List<PythonPackage>>? {
        return try {
            getPackageManager()?.reloadPackages()?.getOrThrow()?.let { Result.success(it) }
        } catch (e: Exception) {
            // e.g. org.apache.hc.client5.http.HttpHostConnectException thrown when docker (in given SDK) is unavailable
            Result.failure(e)
        }
    }

    override fun isWSL() =
        (project.pythonSdk?.sdkAdditionalData as? RemoteSdkProperties)?.sdkId?.startsWith("WSL") ?: false

    override fun checkInstalledRequirement(): Result<Unit> {
        val requirement = getRequirement()
        val packageManager =
            getPackageManager() ?: return Result.failure(UnsupportedOperationException("No package manager found"))
        val installedPackage = packageManager.getInstalledPackageSnapshot(requirement.name) ?: return Result.failure(
            PluginPackageManagementService.PluginPackageManagementException.PackageNotInstalledException()
        )
        if (!getRequirement().match(PyPackage(installedPackage.name, installedPackage.version))) {
            return Result.failure(PluginPackageManagementService.PluginPackageManagementException.PackageVersionObsoleteException())
        }
        return Result.success(Unit)
    }

    override suspend fun installRequirement(): Result<Unit> {
        val packageManager = getPackageManager()!!
        val requirement = getRequirement()
        val versionSpec = requirement.versionSpecs.firstOrNull()
        val specification = PythonRepositoryPackageSpecification(
            packageManager.repositoryManager.repositories.first(), requirement.name, versionSpec
        )
        try {
            packageManager.installPackage(specification.toInstallRequest(), options = emptyList()).getOrThrow()
        } catch (ex: PyExecutionException) {
            return Result.failure(ex)
        }
        return Result.success(Unit)
    }

    private fun getPackageManager(): PythonPackageManager? {
        return project.pythonSdk?.let { PythonPackageManager.forSdk(project, it) }
    }
}
