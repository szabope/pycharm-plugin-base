package works.szabope.plugins.common.run.sh

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import works.szabope.plugins.common.CommonBundle

class ShRunConfiguration(project: Project, factory: ConfigurationFactory) :
    RunConfigurationBase<RunConfiguration>(
        project,
        factory,
        CommonBundle.message("sh.run.configuration.description.0.configuration", ShLanguage.INSTANCE.id)
    ) {
    lateinit var parameters: List<String>
    lateinit var command: String
    lateinit var interpreterPath: String
    var workingDirectory: String? = null

    override fun getState(executor: Executor, environment: ExecutionEnvironment) =
        ShRunConfigurationProfileState(environment.project, this)

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        throw UnsupportedOperationException()
    }
}