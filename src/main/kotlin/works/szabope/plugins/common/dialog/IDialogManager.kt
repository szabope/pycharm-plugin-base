package works.szabope.plugins.common.dialog

import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.PluginPackageManagementException

interface PluginDialog {
    fun show()
    fun getExitCode(): Int = 0
}

interface IDialogManager {
    fun showDialog(dialog: PluginDialog)

    fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException): PluginDialog

    fun createToolExecutionErrorDialog(
        configuration: ImmutableSettingsData,
        result: String,
        resultCode: Int
    ): PluginDialog

    fun createFailedToExecuteErrorDialog(message: String): PluginDialog

    fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData, targets: String, json: String, error: String
    ): PluginDialog

    fun createGeneralErrorDialog(failure: Throwable): PluginDialog

    interface IShowDialog {
        fun showPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
            with(dialogManager) {
                val dialog = createPyPackageInstallationErrorDialog(exception)
                showDialog(dialog)
            }

        fun showGeneralErrorDialog(failure: Throwable) = with(dialogManager) {
            val dialog = createGeneralErrorDialog(failure)
            showDialog(dialog)
        }

        fun showToolExecutionErrorDialog(configuration: ImmutableSettingsData, result: String, resultCode: Int) =
            with(dialogManager) {
                val dialog = createToolExecutionErrorDialog(configuration, result, resultCode)
                showDialog(dialog)
            }

        fun showFailedToExecuteErrorDialog(message: String) = with(dialogManager) {
            val dialog = createFailedToExecuteErrorDialog(message)
            showDialog(dialog)
        }

        fun showToolOutputParseErrorDialog(
            configuration: ImmutableSettingsData, targets: String, json: String, error: String
        ) = with(dialogManager) {
            val dialog = createToolOutputParseErrorDialog(configuration, targets, json, error)
            showDialog(dialog)
        }

        val dialogManager: IDialogManager
    }
}
