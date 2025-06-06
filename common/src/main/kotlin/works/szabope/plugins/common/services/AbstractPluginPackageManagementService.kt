package works.szabope.plugins.common.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Version
import com.intellij.remote.RemoteSdkProperties
import com.jetbrains.python.packaging.PyExecutionException
import com.jetbrains.python.packaging.PyPackage
import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.common.PythonSimplePackageSpecification
import com.jetbrains.python.packaging.management.PythonPackageManager
import com.jetbrains.python.sdk.PythonSdkUtil
import com.jetbrains.python.sdk.pythonSdk

@Suppress("UnstableApiUsage")
abstract class AbstractPluginPackageManagementService : PluginPackageManagementService {

    protected abstract val project: Project

    override fun canInstall(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return !PythonSdkUtil.isRemote(sdk) && !isInstalled()
    }

    override fun isLocalEnvironment(): Boolean {
        val sdk = project.pythonSdk ?: return false
        return PythonSdkUtil.isVirtualEnv(sdk) || PythonSdkUtil.isCondaVirtualEnv(sdk)
    }

    override suspend fun reloadPackages() = try {
        getPackageManager()?.reloadPackages()
    } catch (e: Exception) {
        // e.g. org.apache.hc.client5.http.HttpHostConnectException thrown when docker (in given SDK) is unavailable
        Result.failure(e)
    }

    override fun getInstalledVersion(): Version? {
        return getPackageManager()?.installedPackages?.firstOrNull { it.name == getRequirement().name }?.version?.let {
            Version.parseVersion(
                it
            )
        }
    }

    override fun isVersionSupported(version: Version): Boolean {
        return getRequirement().match(
            mutableListOf(
                PyPackage(
                    getRequirement().name, "${version.major}.${version.minor}.${version.bugfix}"
                )
            )
        ) != null
    }

    override fun isWSL() =
        (project.pythonSdk?.sdkAdditionalData as? RemoteSdkProperties)?.sdkId?.startsWith("WSL") ?: false

    fun getPackageManager(): PythonPackageManager? {
        return project.pythonSdk?.let { PythonPackageManager.forSdk(project, it) }
    }

    override fun isInstalled(): Boolean {
        return getInstalledVersion() != null
    }

    override suspend fun installRequirement(): Result<Unit> {
        val packageManager = getPackageManager()!!
        val requirement = getRequirement()
        val versionSpec = requirement.versionSpecs.firstOrNull()
        val specification = PythonSimplePackageSpecification(
            requirement.name, versionSpec?.version, null, versionSpec?.relation
        )
        try {
            packageManager.installPackage(specification, options = emptyList(), true).getOrThrow()
        } catch (ex: PyExecutionException) {
            return Result.failure(ex)
        }
        return Result.success(Unit)
    }

    protected abstract fun getRequirement(): PyRequirement
}
