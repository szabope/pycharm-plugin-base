@file:Suppress("UnstableApiUsage")

package works.szabope.plugins.common.services

import com.jetbrains.python.packaging.PyRequirement
import com.jetbrains.python.packaging.common.PythonPackage

interface PluginPackageManagementService {
    fun canInstall(): Boolean
    fun isLocalEnvironment(): Boolean
    suspend fun reloadPackages(): Result<List<PythonPackage>>?
    fun isWSL(): Boolean
    fun checkInstalledRequirement(): Result<Unit>
    suspend fun installRequirement(): Result<Unit>
    fun getRequirement(): PyRequirement

    sealed class PluginPackageManagementException : RuntimeException() {
        class PackageNotInstalledException : PluginPackageManagementException()
        class PackageVersionObsoleteException : PluginPackageManagementException()
    }
}