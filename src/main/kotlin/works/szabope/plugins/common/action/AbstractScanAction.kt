package works.szabope.plugins.common.action

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.WriteIntentReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.progress.currentThreadCoroutineScope
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindowManager
import com.jetbrains.python.PythonFileType
import com.jetbrains.python.pyi.PyiFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import works.szabope.plugins.common.services.ImmutableSettingsData
import works.szabope.plugins.common.services.Settings
import works.szabope.plugins.common.toolWindow.ITreeService

val SUPPORTED_FILE_TYPES = arrayOf(PythonFileType.INSTANCE, PyiFileType.INSTANCE)

abstract class AbstractScanAction : DumbAwareAction() {

    abstract fun getTreeService(project: Project): ITreeService
    abstract fun getSettings(project: Project): Settings
    abstract fun getScanJobRegistry(project: Project): AbstractScanJobRegistry
    abstract fun getToolWindowId(): String

    abstract suspend fun scanAndAdd(
        project: Project,
        targets: Collection<VirtualFile>,
        configuration: ImmutableSettingsData,
        treeService: ITreeService
    )

    override fun actionPerformed(event: AnActionEvent) {
        val targets = listTargets(event) ?: return
        val project = event.project ?: return
        val treeService = getTreeService(project)
        treeService.reinitialize(targets)
        @Suppress("UnstableApiUsage")
        WriteIntentReadAction.run { FileDocumentManager.getInstance().saveAllDocuments() }
        val job = currentThreadCoroutineScope().launch(Dispatchers.IO) {
            val configuration = getSettings(project).getValidConfiguration().getOrNull() ?: return@launch
            scanAndAdd(project, targets, configuration, treeService)
            treeService.lock()
        }
        getScanJobRegistry(project).set(job)
        ToolWindowManager.getInstance(project).getToolWindow(getToolWindowId())?.show()
    }

    override fun update(event: AnActionEvent) {
        val targets = listTargets(event) ?: return
        event.presentation.isEnabled = event.project?.let { isReadyToScan(it, targets) } == true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    protected open fun listTargets(event: AnActionEvent): Collection<VirtualFile>? {
        return event.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.asList()
    }

    private fun isReadyToScan(project: Project, targets: Collection<VirtualFile>): Boolean {
        return targets.isNotEmpty() && getScanJobRegistry(project).isAvailable() && isEligibleTargets(targets) && currentThreadCoroutineScope().future {
            getSettings(
                project
            ).getValidConfiguration().isSuccess
        }.get()
    }

    private fun isEligibleTargets(targets: Collection<VirtualFile>) = targets.map { isEligible(it) }.all { it }

    private fun isEligible(virtualFile: VirtualFile) =
        virtualFile.fileType in SUPPORTED_FILE_TYPES || virtualFile.isDirectory
}
