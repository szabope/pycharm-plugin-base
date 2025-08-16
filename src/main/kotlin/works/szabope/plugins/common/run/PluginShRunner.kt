package works.szabope.plugins.common.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.runners.AsyncProgramRunner
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.sh.run.ShRunConfiguration
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise

class PluginShRunner : AsyncProgramRunner<RunnerSettings>() {
    override fun getRunnerId() = "works.szabope.plugins.common.run.PluginShRunner"

    override fun canRun(executorId: String, profile: RunProfile) = profile is ShRunConfiguration

    override fun execute(environment: ExecutionEnvironment, state: RunProfileState): Promise<RunContentDescriptor?> {
        val promise: AsyncPromise<RunContentDescriptor?> = AsyncPromise()
        ApplicationManager.getApplication().invokeAndWait {
            try {
                FileDocumentManager.getInstance().saveAllDocuments()
                val executionResult = state.execute(environment.executor, this)
                ApplicationManager.getApplication()
                    .invokeLater(
                        { promise.setResult(executionResult?.let(RunContentDescriptorFactory::newFakeDescriptor)) },
                        ModalityState.any()
                    )
            } catch (e: ExecutionException) {
                promise.setError(e)
            }
        }
        return promise
    }

    companion object {
        @JvmStatic
        val INSTANCE = PluginShRunner()
    }
}