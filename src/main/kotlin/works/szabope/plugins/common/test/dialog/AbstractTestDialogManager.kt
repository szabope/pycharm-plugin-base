package works.szabope.plugins.common.test.dialog

import com.intellij.openapi.ui.DialogWrapper
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.dialog.PluginDialog
import kotlin.collections.get

abstract class AbstractTestDialogManager : IDialogManager {

    private val myHandlers = hashMapOf<Class<out DialogWrapper>, (TestDialogWrapper) -> Int>()
    private var myAnyHandler: ((TestDialogWrapper) -> Int)? = null

    override fun showDialog(dialog: PluginDialog) {
        val testDialog = dialog as TestDialogWrapper
        testDialog.show()
        var exitCode: Int? = null
        try {
            exitCode = myHandlers[testDialog.getWrappedClass()]?.invoke(testDialog) ?: myAnyHandler?.invoke(testDialog)
            if (exitCode == null) {
                throw IllegalStateException("The dialog is not expected here: $dialog")
            }
        } finally {
            testDialog.close(exitCode ?: DialogWrapper.OK_EXIT_CODE)
        }
    }

    fun onDialog(dialogClass: Class<out DialogWrapper>, handler: (TestDialogWrapper) -> Int) {
        check(myHandlers.put(dialogClass, handler) == null)
    }

    fun onAnyDialog(handler: (TestDialogWrapper) -> Any) {
        myAnyHandler = fun(h: TestDialogWrapper): Int {
            val res = handler.invoke(h)
            return res as? Int ?: DialogWrapper.OK_EXIT_CODE
        }
    }

    fun cleanup() {
        myHandlers.clear()
        myAnyHandler = null
    }
}