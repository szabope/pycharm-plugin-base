package works.szabope.plugins.common.run

import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.target.TargetProgressIndicator
import com.intellij.execution.target.local.LocalTargetEnvironmentRequest
import com.intellij.execution.target.value.constant
import com.intellij.openapi.project.Project
import com.jetbrains.python.console.addDefaultEnvironments
import com.jetbrains.python.run.PythonModuleExecution
import com.jetbrains.python.run.buildTargetedCommandLine
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.python.sdk.targetEnvConfiguration
import works.szabope.plugins.common.CommonBundle
import java.nio.charset.Charset

class PythonModuleExecutionStrategy(
    project: Project,
    moduleToRun: String,
    parameters: List<String> = emptyList(),
    envs: Map<String, String> = emptyMap()
) : ToolExecutionStrategy {
    override val processHandler: OSProcessHandler

    init {
        val sdk = requireNotNull(project.pythonSdk) { CommonBundle.message("tool_executor.python_sdk_null") }
        val targetEnvConfiguration = sdk.targetEnvConfiguration
        val execution = PythonModuleExecution()
        execution.moduleName = moduleToRun
        execution.parameters += parameters.map { constant(it) }

        val patchedEnvs = addDefaultEnvironments(sdk, envs.toMutableMap())
        patchedEnvs.forEach {
            execution.addEnvironmentVariable(it.key, it.value)
        }

        val request = targetEnvConfiguration?.createEnvironmentRequest(project) ?: LocalTargetEnvironmentRequest()
        val targetEnvironment = request.prepareEnvironment(TargetProgressIndicator.EMPTY)

        val targetCommandLine = execution.buildTargetedCommandLine(
            targetEnvironment = targetEnvironment,
            sdk = sdk,
            interpreterParameters = listOf(),
            isUsePty = false,
        )
        val process = targetEnvironment.createProcess(targetCommandLine)
        processHandler = ToolProcessHandler(
            process, targetCommandLine.getCommandPresentation(targetEnvironment), Charset.defaultCharset()
        )
    }
}