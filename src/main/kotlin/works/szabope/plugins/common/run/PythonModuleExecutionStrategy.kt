package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.project.Project
import com.jetbrains.python.console.addDefaultEnvironments
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.CommonBundle
import java.nio.file.Path

fun pythonModuleProcessHandler(
    project: Project,
    moduleToRun: String,
    parameters: List<String> = emptyList(),
    envs: Map<String, String> = emptyMap(),
    workingDir: String?
): OSProcessHandler {
    val sdk = requireNotNull(project.pythonSdk) { CommonBundle.message("tool_executor.python_sdk_null") }
    val patchedEnvs = addDefaultEnvironments(sdk, envs.toMutableMap())
    val commandLine = GeneralCommandLine()
    commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
    commandLine.withWorkingDirectory(workingDir?.let { Path.of(it) })
    commandLine.withExePath(requireNotNull(sdk.homePath) { CommonBundle.message("tool_executor.python_sdk_null") })
    commandLine.withParameters("-m", moduleToRun)
    commandLine.withParameters(parameters)
    commandLine.withEnvironment(patchedEnvs)
    return ToolProcessHandler(commandLine)
}