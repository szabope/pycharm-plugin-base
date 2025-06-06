package works.szabope.plugins.pylint.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.pylint.PylintBundle

data class PylintErrorDescription(
    @DetailedDescription val details: String, @DetailedDescription val message: String? = null
)

class PylintPackageInstallationErrorDialog(message: String) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.installation_error.title"),
    PylintErrorDescription(PylintBundle.message("pylint.dialog.installation_error.details", message))
)

class PylintPackageNotFoundDialog(message: String) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.package_not_found.title"),
    PylintErrorDescription(PylintBundle.message("pylint.dialog.package_not_found.details", message))
)

class PylintExecutionErrorDialog(command: String, result: String, resultCode: Int) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.execution_error.title"), PylintErrorDescription(
        PylintBundle.message("pylint.dialog.execution_error.content", command, result),
        PylintBundle.message("pylint.dialog.execution_error.status_code", resultCode)
    )
)

class PylintParseErrorDialog(configuration: ImmutableSettingsData, targets: String, json: String, error: String) :
    PylintErrorDialog(
        PylintBundle.message("pylint.dialog.parse_error.title"), PylintErrorDescription(
            PylintBundle.message("pylint.dialog.parse_error.details", configuration, targets, json),
            PylintBundle.message("pylint.dialog.parse_error.message", error)
        )
    )

class PylintGeneralErrorDialog(throwable: Throwable) : PylintErrorDialog(
    PylintBundle.message("pylint.dialog.general_error.title"), PylintErrorDescription(
        PylintBundle.message(
            "pylint.dialog.general_error.details", throwable.message!!, throwable.stackTraceToString()
        ), PylintBundle.message(PylintBundle.message("pylint.please_report_this_issue"))
    )
)

open class PylintErrorDialog(
    title: @DialogTitle String, private val description: PylintErrorDescription
) : DialogWrapper(false) {

    init {
        super.init()
        super.setTitle(title)
        super.setErrorText(description.message)
    }

    override fun createCenterPanel() = panel {
        row {
            textArea().applyToComponent {
                text = description.details
                isEditable = false
                columns = COLUMNS_LARGE
                lineWrap = true
            }.align(Align.FILL)
        }.layout(RowLayout.PARENT_GRID)
    }
}
