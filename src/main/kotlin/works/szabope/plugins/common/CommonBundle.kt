package works.szabope.plugins.common

import com.intellij.DynamicBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

@NonNls
private const val BUNDLE = "messages.works_szabope_plugins_CommonBundle"

object CommonBundle {

    private val bundle = DynamicBundle(CommonBundle::class.java, BUNDLE)

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any) = bundle.getMessage(key, *params)

}