package works.szabope.plugins.common.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService

abstract class AbstractInstallToolAction(private val messageInstalled: String) : DumbAwareAction() {

    abstract val toolWindowId: String
    abstract fun getPackageManager(project: Project): AbstractPluginPackageManagementService
    abstract fun handleFailure(failure: Throwable)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        currentThreadCoroutineScope().launch {
            val result = withContext(Dispatchers.Default) {
                getPackageManager(project).installRequirementWithCallback {
                    notifyPanel(project, messageInstalled)
                    e.getData(Notification.KEY)?.expire()
                }
            }
            result.onFailure(::handleFailure)
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { getPackageManager(it).canInstallNow() } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    fun notifyPanel(project: Project, message: String) {
        ToolWindowManager.getInstance(project).notifyByBalloon(toolWindowId, MessageType.INFO, message)
    }
}