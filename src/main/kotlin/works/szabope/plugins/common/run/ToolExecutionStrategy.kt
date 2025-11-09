package works.szabope.plugins.common.run

import com.intellij.execution.process.OSProcessHandler

interface ToolExecutionStrategy {
    val processHandler: OSProcessHandler
}
