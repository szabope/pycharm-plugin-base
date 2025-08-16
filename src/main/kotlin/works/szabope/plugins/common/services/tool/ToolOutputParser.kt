package works.szabope.plugins.common.services.tool

import kotlinx.coroutines.flow.Flow
import works.szabope.plugins.common.services.ToolResultItem

interface ToolOutputParser<I: ToolResultItem> {
    suspend fun parse(stdout: Flow<String>): Result<Flow<I>>
}