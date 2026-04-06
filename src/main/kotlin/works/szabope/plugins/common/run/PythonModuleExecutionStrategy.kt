package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.openapi.project.Project
import com.jetbrains.python.console.addDefaultEnvironments
import works.szabope.plugins.common.CommonBundle
import works.szabope.plugins.common.resolveModulePythonSdkNow
import java.nio.file.Path

fun pythonModuleProcessHandler(
    project: Project,
    moduleToRun: String,
    parameters: List<String> = emptyList(),
    envs: Map<String, String> = emptyMap(),
    workingDir: String?
): OSProcessHandler {
    val sdk = requireNotNull(project.resolveModulePythonSdkNow()) { CommonBundle.message("tool_executor.python_sdk_null") }
    val patchedEnvs = addDefaultEnvironments(sdk, envs.toMutableMap())
    val commandLine = GeneralCommandLine().apply {
        withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        withWorkingDirectory(workingDir?.let { Path.of(it) })
        withExePath(requireNotNull(sdk.homePath) { CommonBundle.message("tool_executor.python_sdk_null") })
        withParameters("-m", moduleToRun)
        withParameters(parameters)
        withEnvironment(patchedEnvs)
    }
    return ToolProcessHandler(commandLine)
}