package works.szabope.plugins.common.dialog

import com.intellij.openapi.ui.DialogWrapper

abstract class AbstractDialogManager : IDialogManager {
    override fun showDialog(dialog: PluginDialog) = dialog.show()

    protected fun DialogWrapper.asPluginDialog() = object : PluginDialog {
        override fun show() = this@asPluginDialog.show()
    }
}
