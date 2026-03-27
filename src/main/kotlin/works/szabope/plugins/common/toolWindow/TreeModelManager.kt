package works.szabope.plugins.common.toolWindow

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.treeStructure.Tree
import works.szabope.plugins.common.CommonBundle
import kotlin.time.measureTime

class TreeModelManager(severities: Set<String>) {

    private val displayedSeverityLevels = severities.toMutableSet()
    private val changeListeners = mutableSetOf<() -> Unit>()
    private val issues = mutableSetOf<TreeModelDataItem>()
    private val model = ToolWindowTreeModel(CommonBundle.message("toolwindow.name.empty"))

    fun add(issue: TreeModelDataItem) {
        issues.add(issue)
        if (isDisplayed(issue)) {
            addToTree(issue)
            updateTree()
            thisLogger().debug("Issue added to tree: $issue")
        }
    }

    fun reload() {
        resetRoot()
        issues.filter { isDisplayed(it) }.forEach { addToTree(it) }
        updateTree()
    }

    fun reinitialize(targets: Collection<VirtualFile>) {
        issues.clear()
        resetRoot(targets)
    }

    fun getRootScanPaths(): Collection<VirtualFile> {
        return model.root.targets
    }

    fun install(tree: Tree) {
        tree.model = model
    }

    fun addChangeListener(listener: () -> Unit) {
        changeListeners.add(listener)
    }

    fun isSeverityLevelDisplayed(severityLevel: String): Boolean {
        return displayedSeverityLevels.contains(severityLevel)
    }

    fun setSeverityLevelDisplayed(severityLevel: String, isDisplayed: Boolean) {
        val hadEffect = if (isDisplayed) {
            displayedSeverityLevels.add(severityLevel)
        } else {
            displayedSeverityLevels.remove(severityLevel)
        }
        if (hadEffect) {
            reload()
        }
    }

    fun updateTree() {
        measureTime {
            model.updateRootText(
                CommonBundle.message(
                    "toolwindow.root.message", getIssueCount(), model.getChildCount(model.root)
                )
            )
            triggerChangeListeners()
        }.let { thisLogger().debug("TreeModelManager#updateTree took $it") }
    }

    private fun triggerChangeListeners() {
        changeListeners.forEach { it() }
    }

    private fun resetRoot(targetsMaybe: Collection<VirtualFile>? = null) {
        val targets = targetsMaybe ?: model.root.targets
        model.setRoot(RootNode("...", targets))
    }

    private fun addToTree(issue: TreeModelDataItem) {
        val fileNode = findOrAddFileNode(issue.file)
        val issueNode = IssueNode(issue)
        model.append(issueNode, fileNode)
        model.root.registerIssueAdded()
    }

    private fun getIssueCount(): Int = model.root.getIssueCount()

    private fun isDisplayed(issue: TreeModelDataItem): Boolean {
        return isSeverityLevelDisplayed(issue.severity.level)
    }

    private fun findOrAddFileNode(file: String): StringNode {
        var fileNode = model.findFileNode(file)
        if (fileNode == null) {
            fileNode = StringNode(file)
            model.append(fileNode, model.root)
        }
        return fileNode
    }
}
