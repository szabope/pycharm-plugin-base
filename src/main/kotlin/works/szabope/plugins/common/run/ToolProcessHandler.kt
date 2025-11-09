package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.util.io.BaseDataReader
import com.intellij.util.io.BaseOutputReader
import java.lang.Boolean
import java.nio.charset.Charset
import kotlin.String

class ToolProcessHandler : OSProcessHandler {
    constructor(commandLine: GeneralCommandLine) : super(commandLine)
    constructor(process: Process, commandLine: String, charset: Charset) : super(process, commandLine, charset)

    override fun readerOptions() = object : BaseOutputReader.Options() {
        override fun policy(): BaseDataReader.SleepingPolicy {
            return if (Boolean.getBoolean("output.reader.blocking.mode")) {
                BLOCKING
            } else {
                NON_BLOCKING
            }.policy()
        }

        override fun splitToLines() = true

        override fun sendIncompleteLines() = false
    }
}