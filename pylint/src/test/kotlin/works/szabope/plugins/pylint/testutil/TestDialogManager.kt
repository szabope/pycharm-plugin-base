// inspired by idea/243.19420.21 git4idea.test.TestDialogManager
package works.szabope.plugins.pylint.testutil

import com.jetbrains.python.packaging.PyExecutionException
import works.szabope.plugins.common.dialog.AbstractTestDialogManager
import works.szabope.plugins.common.dialog.TestDialogWrapper
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.pylint.dialog.*

class TestDialogManager : AbstractTestDialogManager() {
    override fun createPyPackageInstallationErrorDialog(exception: PyExecutionException) = TestDialogWrapper(
        PylintPackageInstallationErrorDialog::class.java, exception
    )

    override fun createToolExecutionErrorDialog(command: String, result: String, resultCode: Int) =
        TestDialogWrapper(PylintExecutionErrorDialog::class.java, command, result, resultCode)

    override fun createToolOutputParseErrorDialog(
        configuration: ImmutableSettingsData, targets: String, json: String, error: String
    ) = TestDialogWrapper(PylintParseErrorDialog::class.java, configuration, targets, json, error)

    override fun createPackageNotFoundDialog(message: String) =
        TestDialogWrapper(PylintPackageNotFoundDialog::class.java, message)

    override fun createGeneralErrorDialog(failure: Throwable) =
        TestDialogWrapper(PylintGeneralErrorDialog::class.java, failure)
}
