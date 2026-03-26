package works.szabope.plugins.common.toolWindow

import com.intellij.ide.projectView.PresentationData
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.Nls
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeNode

class ToolWindowTreeModel(defaultRootNodeText: String) : DefaultTreeModel(RootNode(defaultRootNodeText, emptyList())) {

    override fun setRoot(root: TreeNode?) {
        require(root is RootNode)
        super.setRoot(root)
    }

    override fun getRoot(): RootNode = super.getRoot() as RootNode

    fun append(newNode: DefaultMutableTreeNode, parentNode: DefaultMutableTreeNode) {
        insertNodeInto(newNode, parentNode, getChildCount(parentNode))
    }

    fun updateRootText(message: @Nls String) {
        getRoot().userObject = message
    }

    fun findFileNode(filePath: String): StringNode? {
        for (child in root.children()) {
            if ((child as StringNode).userObject.equals(filePath)) {
                return child
            }
        }
        return null
    }
}

class RootNode(text: String, val targets: Collection<VirtualFile>) : DefaultMutableTreeNode(text, true) {

    private val issueCountStat = AtomicInteger(0)

    fun registerIssueAdded() {
        issueCountStat.incrementAndGet()
    }

    fun getIssueCount(): Int = issueCountStat.get()

    override fun clone(): Any = throw CloneNotSupportedException()
}

class StringNode(text: String) : DefaultMutableTreeNode(text, true) {
    override fun clone(): Any = throw CloneNotSupportedException()
}

class IssueNode(issue: TreeModelDataItem) : DefaultMutableTreeNode(IssueNodeUserObject(issue)) {
    override fun clone(): Any = throw CloneNotSupportedException()
}

class IssueNodeUserObject(issue: TreeModelDataItem) :
    PresentationData(issue.toRepresentation(), null, issue.severity.icon, null), NavigationItem {
    val file = issue.file
    val line = issue.line
    val column = issue.column
    override fun getName(): String? = null
    override fun getPresentation() = this
    override fun toString() = presentableText!!
}
