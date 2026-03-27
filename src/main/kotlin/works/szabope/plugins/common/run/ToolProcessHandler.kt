package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.util.io.BaseDataReader
import com.intellij.util.io.BaseOutputReader

class ToolProcessHandler(commandLine: GeneralCommandLine) : OSProcessHandler(commandLine) {

    override fun readerOptions() = object : BaseOutputReader.Options() {
        override fun policy(): BaseDataReader.SleepingPolicy {
            return if (System.getProperty("output.reader.blocking.mode", "false").toBoolean()) {
                BLOCKING
            } else {
                NON_BLOCKING
            }.policy()
        }

        override fun splitToLines() = true

        override fun sendIncompleteLines() = false
    }
}
