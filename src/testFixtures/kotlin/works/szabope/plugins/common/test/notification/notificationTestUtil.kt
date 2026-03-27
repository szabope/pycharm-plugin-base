package works.szabope.plugins.common.test.notification

import com.intellij.notification.ActionCenter
import com.intellij.notification.Notification
import com.intellij.openapi.project.Project

fun getConfigurationNotCompleteNotification(project: Project, groupId: String, content: String): Notification =
    ActionCenter.getNotifications(project).single {
        groupId == it.groupId && content == it.content && !it.isExpired
    }
