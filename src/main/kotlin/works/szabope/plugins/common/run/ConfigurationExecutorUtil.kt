package works.szabope.plugins.common.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputType
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.util.Key
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class ProcessException(val exitCode: Int, val stdErr: String) :
    RuntimeException("Exit code: $exitCode\nStdErr: $stdErr")

/**
 * @param isMypy Non-zero exit codes may indicate that a result is available on stdout; external errors should not be propagated.
 */
fun execute(environment: ExecutionEnvironment, isMypy: Boolean = false) = callbackFlow {
    val stdErr = StringBuilder()
    val processListener = object : ProcessListener {

        override fun processTerminated(event: ProcessEvent) {
            event.processHandler.removeProcessListener(this)
            if (event.exitCode == 0 || isMypy) {
                close()
            } else {
                close(ProcessException(event.exitCode, stdErr.toString()))
            }
        }

        override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
            if (outputType == ProcessOutputType.STDOUT) {
                event.text?.let { trySend(it) }
            } else if (outputType == ProcessOutputType.STDERR) {
                event.text?.let { stdErr.append(it) }
            }
        }
    }

    environment.callback = ProgramRunner.Callback { descriptor ->
        val processHandler = requireNotNull(descriptor?.processHandler)
        processHandler.addProcessListener(processListener)
    }
    PluginShRunner.INSTANCE.execute(environment)
    awaitClose {}
}
