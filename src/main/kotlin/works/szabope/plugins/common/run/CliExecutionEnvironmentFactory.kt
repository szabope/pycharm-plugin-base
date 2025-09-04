package works.szabope.plugins.common.run

import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.project.Project

class CliExecutionEnvironmentFactory(private val project: Project) {
    fun createEnvironment(command: String): ExecutionEnvironment {
        val conf = ShConfigurationType.INSTANCE.createTemplateConfiguration(project) as ShRunConfiguration
        conf.scriptText = command
        val settings = RunManager.getInstance(project).createConfiguration(conf, conf.factory!!)
        settings.isActivateToolWindowBeforeRun = false
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        return ExecutionEnvironmentBuilder.create(executor, settings).runner(PluginShRunner.INSTANCE).build()
    }
}