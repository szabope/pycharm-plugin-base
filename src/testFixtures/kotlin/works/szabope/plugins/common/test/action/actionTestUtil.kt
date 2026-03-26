package works.szabope.plugins.common.test.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.withCurrentThreadCoroutineScopeBlocking
import java.util.concurrent.ExecutionException

/**
 * Calls [ActionUtil.updateAction] mimicking the production IntelliJ platform contract:
 * - [ActionUpdateThread.BGT] actions: dispatched to a background thread, under ReadAction, with a coroutine scope installed.
 * - EDT actions: called on the current thread, under ReadAction, with a coroutine scope installed.
 */
fun updateActionForTest(action: AnAction, event: AnActionEvent) {
    val doUpdate = Runnable {
        runReadAction {
            withCurrentThreadCoroutineScopeBlocking { ActionUtil.updateAction(action, event) }
        }
    }
    when (action.actionUpdateThread) {
        ActionUpdateThread.BGT -> {
            try {
                ApplicationManager.getApplication().executeOnPooledThread(doUpdate).get()
            } catch (e: ExecutionException) {
                throw e.cause ?: e
            }
        }
        else -> doUpdate.run()
    }
}