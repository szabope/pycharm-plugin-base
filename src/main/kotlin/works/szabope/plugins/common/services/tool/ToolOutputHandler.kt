package works.szabope.plugins.common.services.tool

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import works.szabope.plugins.common.services.ToolResultItem

abstract class ToolOutputHandler<I : ToolResultItem> {

    suspend fun handle(items: Flow<I>) {
        items.onCompletion { throwable -> handleCompletion(throwable) }.collect(::handleResult)
    }

    protected abstract suspend fun handleResult(message: I)
    protected abstract suspend fun handleCompletion(throwable: Throwable?)
}
