package works.szabope.plugins.common.test.toolWindow

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowBalloonShowOptions
import com.intellij.toolWindow.ToolWindowHeadlessManagerImpl
import org.junit.Assert.assertNull

abstract class AbstractTestToolWindowHeadlessManagerImpl(project: Project) :
    ToolWindowHeadlessManagerImpl(project) {

    protected abstract val toolWindowId: String
    private val myHandlers = hashMapOf<String, (ToolWindowBalloonShowOptions) -> Unit>()

    override fun notifyByBalloon(options: ToolWindowBalloonShowOptions) {
        myHandlers[options.toolWindowId]?.invoke(options)
    }

    fun onBalloon(handler: (ToolWindowBalloonShowOptions) -> Unit) {
        assertNull(myHandlers.put(toolWindowId, handler))
    }

    fun cleanup() {
        myHandlers.clear()
    }
}
