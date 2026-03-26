package works.szabope.plugins.common.test.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.webcore.packaging.InstalledPackage
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.PluginPackageManagementException
import java.util.*
import kotlin.Result.Companion.success

abstract class AbstractPluginPackageManagementServiceStub(override val project: Project) :
    AbstractPluginPackageManagementService() {

    // support parallel runs
    private val installedPackagesPerSdk = WeakHashMap<Sdk, MutableList<InstalledPackage>>()

    override suspend fun checkInstalledRequirement(): Result<Unit> {
        val installedPackage =
            getInstalledPackages().firstOrNull { it.name == getRequirement().name } ?: return Result.failure(
                PluginPackageManagementException.PackageNotInstalledException()
            )
        if (!getRequirement().match(PyPackage(installedPackage.name, installedPackage.version ?: ""))) {
            return Result.failure(PluginPackageManagementException.PackageVersionObsoleteException())
        }
        return success(Unit)
    }

    override suspend fun installRequirementWithCallback(callback: () -> Unit): Result<Unit> {
        val r = getRequirement()
        getInstalledPackages().add(InstalledPackage(r.name, r.versionSpecs.firstOrNull()?.version))
        callback()
        return success(Unit)
    }

    private fun getInstalledPackages(): MutableList<InstalledPackage> {
        val sdk = project.pythonSdk ?: return mutableListOf()
        var installedPackages = installedPackagesPerSdk[sdk]
        if (installedPackages == null) {
            installedPackages = mutableListOf()
            installedPackagesPerSdk[sdk] = installedPackages
        }
        return installedPackages
    }
}