package works.szabope.plugins.common.toolWindow

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree

interface ITreeService {
    fun getRootScanPaths(): Collection<VirtualFile>
    fun reinitialize(targets: Collection<VirtualFile>)
    fun addChangeListener(onModelChange: () -> Unit)
    fun isSeverityLevelDisplayed(level: String): Boolean
    fun setSeverityLevelDisplayed(level: String, selected: Boolean)
    fun add(item: TreeModelDataItem)
    fun install(tree: Tree)
    fun lock()
}