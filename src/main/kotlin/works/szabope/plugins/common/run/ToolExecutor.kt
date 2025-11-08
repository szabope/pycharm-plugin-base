package works.szabope.plugins.common.run

import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.services.ImmutableSettingsData

data class ProcessLine(val text: String, val isError: Boolean)
class ToolExecutionTerminatedException(val exitCode: Int) : Exception()

abstract class ToolExecutor(private val project: Project, private val moduleToRun: String) {
    fun execute(
        configuration: ImmutableSettingsData, parameters: List<String> = emptyList()
    ): Flow<ProcessLine> = channelFlow {
        val handler = if (configuration.useProjectSdk) {
            PythonModuleExecutionStrategy(project, moduleToRun, parameters)
        } else {
            CommandLineExecutionStrategy(configuration, parameters)
        }.processHandler

        val listener = object : ProcessListener {
            override fun onTextAvailable(event: ProcessEvent, outputType: com.intellij.openapi.util.Key<*>) {
                when (outputType) {
                    ProcessOutputTypes.STDOUT -> {
                        trySend(ProcessLine(event.text, false))
                    }

                    ProcessOutputTypes.STDERR -> {
                        trySend(ProcessLine(event.text, true))
                    }

                    else -> {}
                }
            }

            override fun processTerminated(event: ProcessEvent) {
                if (event.exitCode > 0) {
                    close(ToolExecutionTerminatedException(event.exitCode))
                } else {
                    close() // signal flow completion
                }
            }
        }

        handler.addProcessListener(listener)
        handler.startNotify()
        // ensure process is cleaned up when cancelled
        awaitClose {
            if (handler.isProcessTerminating || handler.isProcessTerminated) return@awaitClose
            handler.destroyProcess()
        }

        // suspend until process terminates
        withContext(Dispatchers.IO) {
            handler.waitFor()
        }
    }.buffer(Channel.UNLIMITED)
}