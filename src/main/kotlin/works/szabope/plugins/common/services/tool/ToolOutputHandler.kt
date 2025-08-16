package works.szabope.plugins.common.services.tool

import kotlinx.coroutines.flow.Flow
import works.szabope.plugins.common.services.ToolResultItem

abstract class ToolOutputHandler<I : ToolResultItem> {

    suspend fun handle(items: Flow<I>) {
        items.collect { message ->
            handleResult(message)
        }
    }

    protected abstract suspend fun handleResult(message: I)
}
