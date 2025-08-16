package works.szabope.plugins.common.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.openapi.util.Key
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ProcessException(val exitCode: Int, val stdErr: String) :
    RuntimeException("Exit code: $exitCode\nStdErr: $stdErr")

fun ProcessHandler.collectOutput(): Flow<String> = callbackFlow {
    val stdErr = StringBuilder()
    val processListener = object : ProcessListener {

        override fun processTerminated(event: ProcessEvent) {
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
    addProcessListener(processListener)
    awaitClose {
        removeProcessListener(processListener)
    }
}
