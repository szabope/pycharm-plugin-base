package works.szabope.plugins.common.activity

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.services.BasicSettingsData
import works.szabope.plugins.common.services.Settings

abstract class AbstractSettingsInitializationActivity : ProjectActivity {

    abstract fun getPackageManagementService(project: Project): AbstractPluginPackageManagementService
    abstract fun getSettings(project: Project): Settings
    abstract suspend fun getOldSettings(project: Project): BasicSettingsData
    abstract fun notifyIncomplete(project: Project, canInstall: Boolean)

    override suspend fun execute(project: Project) {
        if (project.isDefault) {
            return
        }
        val settings = getSettings(project)
        // we trust in old settings' validity
        settings.initSettings(getOldSettings(project))
        if (settings.getValidConfiguration().isFailure) {
            val canInstall = getPackageManagementService(project).canInstall()
            notifyIncomplete(project, canInstall)
        }
    }
}