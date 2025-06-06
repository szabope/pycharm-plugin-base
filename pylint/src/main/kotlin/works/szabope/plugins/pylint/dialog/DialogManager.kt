// inspired by idea/243.19420.21 git4idea.DialogManager
package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.jetbrains.python.packaging.PyExecutionException
import works.szabope.plugins.common.dialog.IDialogManager
import works.szabope.plugins.common.dialog.PluginDialog
import works.szabope.plugins.common.services.ImmutableSettingsData

private fun DialogWrapper.toPylintDialog() = object : PluginDialog {
    override fun show() = this@toPylintDialog.show()
}

class DialogManager : IDialogManager {
    override fun showDialog(dialog: PluginDialog) = dialog.show()

    override fun createPyPackageInstallationErrorDialog(exception: PyExecutionException) =
        PylintPackageInstallationErrorDialog(exception.message!!).toPylintDialog()


    override fun createToolExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        PylintExecutionErrorDialog(command, result, resultCode).toPylintDialog()

    override fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData,
        targets: String,
        json: String,
        error: String
    ) = PylintParseErrorDialog(configuration, targets, json, error).toPylintDialog()

    override fun createPackageNotFoundDialog(message: String) = PylintPackageNotFoundDialog(message).toPylintDialog()

    override fun createGeneralErrorDialog(failure: Throwable) = PylintGeneralErrorDialog(failure).toPylintDialog()
}
