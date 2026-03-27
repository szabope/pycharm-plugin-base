package works.szabope.plugins.common.test.action

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.ActionUtil.performAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.withCurrentThreadCoroutineScopeBlocking
import com.intellij.testFramework.PlatformTestUtil
import org.junit.Assert
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

fun invokeNamedActionWithScope(actionId: String) {
    val action = ActionManager.getInstance().getAction(actionId)!!
    @Suppress("DEPRECATION") val context = DataManager.getInstance().dataContext
    val event = AnActionEvent.createEvent(action, context, null, "", ActionUiKind.NONE, null)
    PerformWithDocumentsCommitted.commitDocumentsIfNeeded(action, event)
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun waitForIt(actionId: String, context: DataContext) {
    val action = ActionManager.getInstance().getAction(actionId)
    val event = AnActionEvent.createEvent(context, null, "", ActionUiKind.NONE, null)
    PlatformTestUtil.waitWhileBusy {
        updateActionForTest(action, event)
        !event.presentation.isEnabled
    }
}

fun markExcluded(context: DataContext) {
    if (context.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.isNotEmpty() != true) {
        throw IllegalArgumentException("Use `CommonDataKeys.VIRTUAL_FILE_ARRAY` for virtual files to exclude them")
    }
    val event = AnActionEvent.createEvent(context, null, "", ActionUiKind.NONE, null)
    val action = ActionManager.getInstance().getAction("MarkExcludeRoot")
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}

fun unmark(context: DataContext) {
    val event = AnActionEvent.createEvent(context, null, "", ActionUiKind.NONE, null)
    if (event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY).isNullOrEmpty()) {
        throw IllegalArgumentException("Use `CommonDataKeys.VIRTUAL_FILE_ARRAY` for virtual files to (un)mark them")
    }
    val action = ActionManager.getInstance().getAction("UnmarkRoot")
    updateActionForTest(action, event)
    Assert.assertTrue(event.presentation.isEnabled)
    performAction(action, event)
}