package works.szabope.plugins.common.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.KeyboardFocusManager

data class PluginErrorDescription(
    @DetailedDescription val details: String?, @DetailedDescription val message: String? = null
)

open class PluginErrorDialog(
    title: @DialogTitle String, private val description: PluginErrorDescription
) : DialogWrapper(false) {

    private val dialogWidth = (KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .activeWindow?.width?.times(0.75))?.toInt() ?: JBUI.scale(800)

    init {
        setTitle(title)
        super.init()
        setErrorText(description.message)
        contentPanel.preferredSize = Dimension(dialogWidth, 0)
        contentPanel.maximumSize = Dimension(dialogWidth, contentPanel.preferredSize.height)
    }

    override fun createCenterPanel() = description.details?.let { details ->
        panel {
            row {
                textArea().applyToComponent {
                    text = details
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                    setSize(dialogWidth, 0)
                }.align(AlignX.FILL)
            }
        }
    }
}
