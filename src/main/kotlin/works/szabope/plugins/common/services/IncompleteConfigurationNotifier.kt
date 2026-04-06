package works.szabope.plugins.common.services

import com.intellij.notification.ActionCenter
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project

abstract class IncompleteConfigurationNotifier(
    private val project: Project,
    private val notificationGroupName: String,
    private val message: String,
    private val openSettingsActionId: String,
    private val installActionId: String,
) {
    fun showWarningBubble(canInstall: Boolean) {
        ActionCenter.getNotifications(project).filter {
            it.groupId == notificationGroupName && it.content == message && !it.isExpired
        }.forEach { it.expire() }
        val openSettingsAction = ActionManager.getInstance().getAction(openSettingsActionId)
        val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(notificationGroupName)
        val notification =
            notificationGroup.createNotification(message, NotificationType.WARNING).addAction(openSettingsAction)
        if (canInstall) {
            val installAction = ActionManager.getInstance().getAction(installActionId)
            notification.addAction(installAction)
        }
        notification.notify(project)
    }

}