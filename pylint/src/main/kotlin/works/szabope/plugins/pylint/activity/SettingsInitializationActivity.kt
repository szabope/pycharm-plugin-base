package works.szabope.plugins.pylint.activity

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.platform.backend.workspace.workspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import org.jetbrains.annotations.TestOnly
import org.jetbrains.annotations.VisibleForTesting
import works.szabope.plugins.common.services.PluginPackageManagementService
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.pylint.services.IncompleteConfigurationNotificationService
import works.szabope.plugins.pylint.services.OldPylintSettings

internal class SettingsInitializationActivity : ProjectActivity {

    @TestOnly
    val configurationCalled = Channel<Unit>(capacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST)

    override suspend fun execute(project: Project) {
        configurePlugin(project)
        if (!ApplicationManager.getApplication().isUnitTestMode) {
            project.workspaceModel.eventLog.filter {
                it.getChanges(ModuleEntity::class.java).isNotEmpty()
            }.collectLatest {
                configurePlugin(project)
            }
        }
    }

    @VisibleForTesting
    suspend fun configurePlugin(project: Project) {
        PluginPackageManagementService.getInstance(project).reloadPackages()
        val settings = Settings.getInstance(project)
        if (!settings.isComplete()) {
            settings.initSettings(OldPylintSettings.getInstance(project))
        }
        if (!settings.isComplete()) {
            val notificationService = IncompleteConfigurationNotificationService.getInstance(project)
            val canInstall = PluginPackageManagementService.getInstance(project).canInstall()
            notificationService.notify(canInstall)
        }
        configurationCalled.send(Unit)
    }
}
