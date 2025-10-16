package works.szabope.plugins.common.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.future
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService

abstract class AbstractInstallToolAction(private val messageInstalling: String, private val messageInstalled: String) :
    DumbAwareAction() {

    abstract fun getPackageManager(project: Project): AbstractPluginPackageManagementService
    abstract fun handleFailure(failure: Throwable)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val installResult = currentThreadCoroutineScope().future(Dispatchers.Default) {
            getPackageManager(project).installRequirement()
        }
        ApplicationManager.getApplication().invokeLater {
            installResult.get().onFailure(::handleFailure).onSuccess {
                notifyPanel(project, messageInstalled)
                e.getData(Notification.KEY)?.expire()
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