package works.szabope.plugins.common.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.services.PluginPackageManagementService

abstract class AbstractInstallToolAction(private val messageInstalling: String, private val messageInstalled: String) :
    DumbAwareAction() {

    abstract fun getPackageManager(project: Project): PluginPackageManagementService
    abstract fun handleFailure(failure: Throwable)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        runWithModalProgressBlocking(project, messageInstalling) {
            withContext(Dispatchers.EDT) {
                getPackageManager(project).installRequirement().onFailure(::handleFailure).onSuccess {
                    notifyPanel(project, messageInstalled)
                }
            }
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { getPackageManager(it).canInstall() } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    abstract fun notifyPanel(project: Project, message: String)
}