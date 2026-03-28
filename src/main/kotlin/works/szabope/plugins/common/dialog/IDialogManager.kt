package works.szabope.plugins.common.dialog

import works.szabope.plugins.common.services.PluginPackageManagementException
import works.szabope.plugins.common.services.ToolExecutorConfiguration

interface PluginDialog {
    fun show()
    fun getExitCode(): Int = 0
}

interface IDialogManager {
    fun showDialog(dialog: PluginDialog)
    fun createPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException): PluginDialog
    fun createToolExecutionErrorDialog(configuration: ToolExecutorConfiguration, result: String, resultCode: Int?): PluginDialog
    fun createToolOutputParseErrorDialog(configuration: ToolExecutorConfiguration, targets: String, json: String, error: String): PluginDialog
    fun createGeneralErrorDialog(failure: Throwable): PluginDialog

    fun showPyPackageInstallationErrorDialog(exception: PluginPackageManagementException.InstallationFailedException) =
        showDialog(createPyPackageInstallationErrorDialog(exception))

    fun showGeneralErrorDialog(failure: Throwable) =
        showDialog(createGeneralErrorDialog(failure))

    fun showToolExecutionErrorDialog(configuration: ToolExecutorConfiguration, result: String, resultCode: Int?) =
        showDialog(createToolExecutionErrorDialog(configuration, result, resultCode))

    fun showToolOutputParseErrorDialog(configuration: ToolExecutorConfiguration, targets: String, json: String, error: String) =
        showDialog(createToolOutputParseErrorDialog(configuration, targets, json, error))
}
