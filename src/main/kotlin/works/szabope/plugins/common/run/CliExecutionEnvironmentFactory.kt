package works.szabope.plugins.common.run

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionEnvironmentBuilder
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.run.sh.ShConfigurationType
import works.szabope.plugins.common.run.sh.ShRunConfiguration

class CliExecutionEnvironmentFactory(private val project: Project) {
    fun createEnvironment(
        command: String, params: List<String>, workingDirectory: String? = null
    ): ExecutionEnvironment {
        val conf = ShConfigurationType.INSTANCE.createTemplateConfiguration(project) as ShRunConfiguration
        conf.command = command
        conf.parameters = params
        conf.workingDirectory = workingDirectory ?: conf.workingDirectory
        val settings = RunManager.getInstance(project).createConfiguration(conf, conf.factory!!)
        settings.isActivateToolWindowBeforeRun = false
        val executor = DefaultRunExecutor.getRunExecutorInstance()
        return ExecutionEnvironmentBuilder.create(executor, settings)
            // FIXME hack: avoid EDT check that originates from ExecutionManagerImpl#doStartRunProfile
            .contentToReuse(
                RunContentDescriptorFactory.newFakeDescriptor(
                    DefaultExecutionResult()
                )
            ).runner(PluginShRunner.INSTANCE).build()
    }
}