package works.szabope.plugins.common.run

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.util.Key
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class ProcessException(val exitCode: Int, val stdErr: String) :
    RuntimeException("Exit code: $exitCode\nStdErr: $stdErr")

fun execute(environment: ExecutionEnvironment) = callbackFlow {
    val stdErr = StringBuilder()
    val processListener = object : ProcessListener {

        override fun processTerminated(event: ProcessEvent) {
            event.processHandler.removeProcessListener(this)
            if (event.exitCode == 0) {
                close()
            } else {
                close(ProcessException(event.exitCode, stdErr.toString()))
            }
        }

        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
            if (outputType == ProcessOutputType.STDOUT) {
                event.text?.let { trySend(it) }
            } else {
                event.text?.let { stdErr.append(it) }
            }
        }
    }

    ProgramRunnerUtil.executeConfigurationAsync(environment, false, false) { descriptor ->
        val processHandler = requireNotNull(descriptor?.processHandler)
        processHandler.addProcessListener(processListener)
    }
    awaitClose {}
}.flowOn(Dispatchers.IO).buffer(capacity = Channel.UNLIMITED)
