package works.szabope.plugins.common.test.context

import com.intellij.ide.ui.IdeUiService
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

fun dataContext(
    project: Project, toolWindowId: String, customizer: SimpleDataContext.Builder.() -> Unit
): DataContext {
    val panel = ToolWindowManager.getInstance(project)
        .getToolWindow(toolWindowId)!!.contentManager.contents.single().component
    val panelContext = IdeUiService.getInstance().createUiDataContext(panel)
    val testContext = SimpleDataContext.builder().setParent(panelContext).add(CommonDataKeys.PROJECT, project).build()
    val builder = SimpleDataContext.builder().setParent(testContext)
    customizer(builder)
    return builder.build()
}
