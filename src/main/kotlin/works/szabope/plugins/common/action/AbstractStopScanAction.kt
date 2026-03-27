package works.szabope.plugins.common.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import kotlinx.coroutines.launch
import works.szabope.plugins.common.toolWindow.ITreeService

abstract class AbstractStopScanAction : DumbAwareAction() {

    abstract fun getScanJobRegistry(project: Project): AbstractScanJobRegistry
    abstract fun getTreeService(project: Project): ITreeService

    override fun actionPerformed(event: AnActionEvent) {
        currentThreadCoroutineScope().launch {
            val project = event.project ?: return@launch
            getScanJobRegistry(project).cancel()
            getTreeService(project).lock()
        }
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabled = event.project?.let { getScanJobRegistry(it).isActive() } ?: false
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
