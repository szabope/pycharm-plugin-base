package works.szabope.plugins.common.run

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.runners.ExecutionEnvironment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

suspend fun execute(environment: ExecutionEnvironment): Result<Flow<String>> {
    val futureStdOutFlow = CompletableFuture<Flow<String>>()
    ProgramRunnerUtil.executeConfigurationAsync(environment, false, false) { descriptor ->
        try {
            requireNotNull(descriptor?.processHandler).collectOutput().let { flow ->
                futureStdOutFlow.complete(flow)
            }
        } catch (e: Exception) {
            futureStdOutFlow.completeExceptionally(e)
        }
    }
    return futureStdOutFlow.runCatching { await() }
}
