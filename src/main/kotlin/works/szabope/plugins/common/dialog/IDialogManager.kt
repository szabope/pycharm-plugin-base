package works.szabope.plugins.common.dialog

import works.szabope.plugins.common.services.PluginPackageManagementException

interface PluginDialog {
    fun show()
    fun getExitCode(): Int = 0
}

interface IDialogManager {
    fun showDialog(dialog: PluginDialog)
    fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException): PluginDialog
    fun createToolExecutionErrorDialog(commandLine: String, result: String, resultCode: Int?): PluginDialog
    fun createToolOutputParseErrorDialog(commandLine: String, targets: String, json: String, error: String): PluginDialog
    fun createGeneralErrorDialog(failure: Throwable): PluginDialog

    fun showPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        showDialog(createPyPackageInstallationErrorDialog(exception))

    fun showGeneralErrorDialog(failure: Throwable) =
        showDialog(createGeneralErrorDialog(failure))

    fun showToolExecutionErrorDialog(commandLine: String, result: String, resultCode: Int?) =
        showDialog(createToolExecutionErrorDialog(commandLine, result, resultCode))

    fun showToolOutputParseErrorDialog(commandLine: String, targets: String, json: String, error: String) =
        showDialog(createToolOutputParseErrorDialog(commandLine, targets, json, error))
}
