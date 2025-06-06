package works.szabope.plugins.common.dialog

import com.intellij.openapi.components.service
import com.jetbrains.python.packaging.PyExecutionException
import works.szabope.plugins.common.services.ImmutableSettingsData

interface PluginDialog {
    fun show()
    fun getExitCode(): Int = 0
}

interface IDialogManager {
    fun showDialog(dialog: PluginDialog)

    fun createPyPackageInstallationErrorDialog(exception: PyExecutionException): PluginDialog

    fun createToolExecutionErrorDialog(command: String, result: String, resultCode: Int): PluginDialog

    fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData, targets: String, json: String, error: String
    ): PluginDialog

    fun createPackageNotFoundDialog(message: String): PluginDialog

    fun createGeneralErrorDialog(failure: Throwable): PluginDialog

    companion object {
        fun showPyPackageInstallationErrorDialog(exception: PyExecutionException) = with(dialogManager()) {
            val dialog = createPyPackageInstallationErrorDialog(exception)
            showDialog(dialog)
        }

        fun showPackageNotFoundDialog(message: String) = with(dialogManager()) {
            val dialog = createPackageNotFoundDialog(message)
            showDialog(dialog)
        }

        fun showGeneralErrorDialog(failure: Throwable) = with(dialogManager()) {
            val dialog = createGeneralErrorDialog(failure)
            showDialog(dialog)
        }

        fun showToolExecutionErrorDialog(command: String, result: String, resultCode: Int) = with(dialogManager()) {
            val dialog = createToolExecutionErrorDialog(command, result, resultCode)
            showDialog(dialog)
        }

        fun showToolOutputParseErrorDialog(
            configuration: ImmutableSettingsData, targets: String, json: String, error: String
        ) = with(dialogManager()) {
            val dialog = createToolOutputParseErrorDialog(configuration, targets, json, error)
            showDialog(dialog)
        }

        private fun dialogManager(): IDialogManager {
            return service<IDialogManager>()
        }
    }
}