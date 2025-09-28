package works.szabope.plugins.common.test.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.webcore.packaging.InstalledPackage
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.PluginPackageManagementService
import java.util.*
import kotlin.Result.Companion.success

abstract class AbstractPluginPackageManagementServiceStub(override val project: Project) :
    AbstractPluginPackageManagementService() {

    // support parallel runs
    private val installedPackagesPerSdk = WeakHashMap<Sdk, MutableList<InstalledPackage>>()

    override fun checkInstalledRequirement(): Result<Unit> {
        val installedPackage =
            getInstalledPackages().firstOrNull { it.name == getRequirement().name } ?: return Result.failure(
                PluginPackageManagementService.PluginPackageManagementException.PackageNotInstalledException()
            )
        if (!getRequirement().match(PyPackage(installedPackage.name, installedPackage.version ?: ""))) {
            return Result.failure(PluginPackageManagementService.PluginPackageManagementException.PackageVersionObsoleteException())
        }
        return success(Unit)
    }

    override suspend fun installRequirement(): Result<Unit> {
        val r = getRequirement()
        getInstalledPackages().add(InstalledPackage(r.name, r.versionSpecs.firstOrNull()?.version))
        return success(Unit)
    }

    override suspend fun reloadPackages(): Result<List<PythonPackage>> {
        if (project.pythonSdk == null) {
            return success(emptyList())
        }
        return success(getInstalledPackages().map { PythonPackage(it.name, it.version!!, false) }.toList())
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