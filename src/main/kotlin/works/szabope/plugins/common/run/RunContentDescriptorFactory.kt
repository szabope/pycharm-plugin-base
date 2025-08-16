package works.szabope.plugins.common.run

import com.intellij.execution.ExecutionResult
import com.intellij.execution.ui.RunContentDescriptor
import javax.swing.JComponent

object RunContentDescriptorFactory {
    fun newFakeDescriptor(executionResult: ExecutionResult): RunContentDescriptor {
        return object : RunContentDescriptor(
            executionResult.executionConsole,
            executionResult.processHandler,
            object : JComponent() {},
            "F-A-K-E"
        ) {
            @Suppress("UnstableApiUsage")
            override fun isHiddenContent() = true
        }
    }
}
