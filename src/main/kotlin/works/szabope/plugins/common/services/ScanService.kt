package works.szabope.plugins.common.services

import com.intellij.openapi.vfs.VirtualFile
import works.szabope.plugins.common.services.tool.ToolOutputHandler

interface ScanService<I : ToolResultItem> {
    fun scan(
        targets: Collection<VirtualFile>,
        configuration: ImmutableSettingsData,
        resultHandler: ToolOutputHandler<I>
    )
}