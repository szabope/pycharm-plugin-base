package works.szabope.plugins.common.action

import com.intellij.notification.Notification
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.wm.ToolWindowManager

abstract class AbstractOpenSettingsAction : AnAction() {

    abstract fun getConfigurableClass(): Class<out BoundSearchableConfigurable>

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        ToolWindowManager.getInstance(project).invokeLater {
            e.getData(Notification.KEY)?.expire()
            ShowSettingsUtil.getInstance().showSettingsDialog(project, getConfigurableClass())
        }
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
