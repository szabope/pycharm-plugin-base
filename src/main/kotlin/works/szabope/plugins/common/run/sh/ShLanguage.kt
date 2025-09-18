package works.szabope.plugins.common.run.sh

import com.intellij.lang.Language

class ShLanguage : Language(
    "works.szabope.plugins.common|Shell Script", *arrayOf("application/x-bsh", "application/x-sh", "text/x-script.sh")
) {
    companion object {
        @JvmStatic
        val INSTANCE = ShLanguage()
    }
}