package works.szabope.plugins.common.services.tool

import com.intellij.openapi.application.EDT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import works.szabope.plugins.common.messages.MessageConverter
import works.szabope.plugins.common.services.ToolResultItem
import works.szabope.plugins.common.toolWindow.ITreeService
import works.szabope.plugins.common.toolWindow.TreeModelDataItem

abstract class AbstractPublishingToolOutputHandler<I : ToolResultItem>(
    private val converter: MessageConverter<I, TreeModelDataItem>
) : ToolOutputHandler<I>() {

    abstract val treeService: ITreeService

    private fun convert(message: I): TreeModelDataItem = converter.convert(message)

    override suspend fun handleResult(message: I) {
        val item = convert(message)
        withContext(Dispatchers.EDT) {
            treeService.add(item)
        }
    }

    override suspend fun handleCompletion(throwable: Throwable?) {
        treeService.lock()
    }
}
