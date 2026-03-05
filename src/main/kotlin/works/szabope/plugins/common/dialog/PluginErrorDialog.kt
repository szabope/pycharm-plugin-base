package works.szabope.plugins.common.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.NlsContexts.DetailedDescription
import com.intellij.openapi.util.NlsContexts.DialogTitle
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import java.awt.Dimension

data class PluginErrorDescription(
    @DetailedDescription val details: String?, @DetailedDescription val message: String? = null
)

open class PluginErrorDialog(
    title: @DialogTitle String, private val description: PluginErrorDescription
) : DialogWrapper(false) {

    init {
        setTitle(title)
        super.init()
        setErrorText(description.message)
        contentPanel.maximumSize = Dimension(JBUI.scale(800), contentPanel.preferredSize.height)
    }

    override fun createCenterPanel() = description.details?.let { details ->
        panel {
            row {
                textArea().applyToComponent {
                    text = details
                    isEditable = false
                    lineWrap = true
                    wrapStyleWord = true
                    setSize(JBUI.scale(800), 0)
                }.align(AlignX.FILL)
            }
        }
    }
}
