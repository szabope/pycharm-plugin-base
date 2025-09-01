package works.szabope.plugins.common.test.dialog

import works.szabope.plugins.common.dialog.PluginDialog

class TestDialogWrapper(private val lie: Class<out Any>, vararg args: Any) : PluginDialog {
    private val arguments = args
    private var isShown = false
    private var exitCode: Int? = null

    fun getWrappedClass() = lie

    override fun show() {
        isShown = true
    }

    fun close(exitCode: Int) {
        this.exitCode = exitCode
    }

    fun isShown(): Boolean = isShown

    override fun getExitCode(): Int = requireNotNull(exitCode)

    override fun toString() =
        "\nDialog: ${getWrappedClass().simpleName}\nArguments:\n\t${arguments.joinToString("\n\t")}\n"
}