package works.szabope.plugins.common.services

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.wm.ToolWindowManager
import javax.swing.event.HyperlinkEvent

fun showClickableBalloonError(project: Project, toolWindowId: String, balloonMessage: String, onClick: () -> Unit) {
    ToolWindowManager.getInstance(project).notifyByBalloon(toolWindowId, MessageType.ERROR, balloonMessage, null) {
        if (it.eventType == HyperlinkEvent.EventType.ACTIVATED) onClick()
    }
}
