@file:Suppress("UnstableApiUsage")

package common.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.Version
import com.jetbrains.python.packaging.common.PythonPackage

interface PluginPackageManagementService {
    class PackageNotFoundException(message: @NlsSafe String) : Exception(message)

    fun canInstall(): Boolean
    fun isLocalEnvironment(): Boolean

    suspend fun reloadPackages(): Result<List<PythonPackage>>?
    fun getInstalledVersion(): Version?
    fun isVersionSupported(version: Version): Boolean
    fun isInstalled(): Boolean
    fun isWSL(): Boolean

    suspend fun installRequirement(): Result<Unit>

    companion object {
        @JvmStatic
        fun getInstance(project: Project): PluginPackageManagementService = project.service()
    }
}