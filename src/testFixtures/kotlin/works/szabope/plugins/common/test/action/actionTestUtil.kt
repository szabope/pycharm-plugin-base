package works.szabope.plugins.common.test.action

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.ex.ActionUtil.performAction
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.withCurrentThreadCoroutineScopeBlocking
import com.intellij.testFramework.PlatformTestUtil
import org.junit.Assert

/**
 * Calls [ActionUtil.updateAction] on the current thread, under ReadAction, with a coroutine scope installed,
 * regardless of the action's [ActionUpdateThread].
 *
 * Same as the platform's own test helpers (e.g. `PlatformTestUtil.invokeNamedAction`). [ActionUpdateThread.BGT]
 * actions are intentionally not dispatched to a background thread: tests run on EDT holding the write-intent lock,
 * so blocking EDT on a pooled thread that needs a read lock deadlocks as soon as a background write action
 * (e.g. indexing scan) becomes pending.
 */
fun updateActionForTest(action: AnAction, event: AnActionEvent) {
    runReadAction {
        withCurrentThreadCoroutineScopeBlocking { ActionUtil.updateAction(action, event) }
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