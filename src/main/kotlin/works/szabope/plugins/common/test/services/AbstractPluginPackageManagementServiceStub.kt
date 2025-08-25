package works.szabope.plugins.common.test.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.util.Version
import com.intellij.webcore.packaging.InstalledPackage
import com.jetbrains.python.packaging.common.PythonPackage
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import java.util.*
import kotlin.Result.Companion.success

abstract class AbstractPluginPackageManagementServiceStub(override val project: Project) :
    AbstractPluginPackageManagementService() {

    // support parallel runs
    private val installedPackagesPerSdk = WeakHashMap<Sdk, MutableList<InstalledPackage>>()

    override fun getInstalledVersion(): Version? {
        return getInstalledPackages().firstOrNull { it.name == getRequirement().name }?.version?.let {
            Version.parseVersion(
                it
            )
        }
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