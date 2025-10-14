package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.project.Project
import com.jetbrains.python.sdk.PythonExecuteUtils
import com.jetbrains.python.sdk.pythonSdk
import works.szabope.plugins.common.CommonBundle
import works.szabope.plugins.common.services.ImmutableSettingsData
import java.nio.file.Path
import kotlin.time.Duration

abstract class ToolExecutor(private val project: Project, private val moduleToRun: String) {
    fun execute(configuration: ImmutableSettingsData, parameters: List<String> = emptyList()): Result<ProcessOutput> {
        if (configuration.useProjectSdk) {
            val pythonSdk = requireNotNull(project.pythonSdk) { CommonBundle.message("tool_executor.python_sdk_null") }
            return runCatching {
                PythonExecuteUtils.executePyModuleScript(
                    project, pythonSdk, moduleToRun, parameters, timeout = Duration.INFINITE
                )
            }
        } else {
            val commandLine = GeneralCommandLine()
            commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            commandLine.withWorkingDirectory(configuration.projectDirectory?.let { Path.of(it) })
            commandLine.withExePath(configuration.executablePath)
            commandLine.withParameters(parameters)
            return runCatching { ExecUtil.execAndGetOutput(commandLine) }
        }
    }
}