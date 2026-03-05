package works.szabope.plugins.common.services

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project

fun notifyIncompleteConfiguration(
    project: Project,
    notificationGroupName: String,
    message: String,
    openSettingsActionId: String,
    installActionId: String,
    canInstall: Boolean
) {
    val openSettingsAction = ActionManager.getInstance().getAction(openSettingsActionId)
    val notificationGroup = NotificationGroupManager.getInstance().getNotificationGroup(notificationGroupName)
    val notification = notificationGroup.createNotification(message, NotificationType.WARNING).addAction(openSettingsAction)
    if (canInstall) {
        val installAction = ActionManager.getInstance().getAction(installActionId)
        notification.addAction(installAction)
    }
    notification.notify(project)
}
