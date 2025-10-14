package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessOutput
import com.intellij.execution.util.ExecUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.jetbrains.python.sdk.PythonExecuteUtils
import works.szabope.plugins.common.services.ImmutableSettingsData
import java.nio.file.Path
import kotlin.time.Duration

open class ToolExecutor(private val project: Project, private val pythonSdk: Sdk, private val moduleToRun: String) {
    fun execute(configuration: ImmutableSettingsData, parameters: List<String> = emptyList()): ProcessOutput {
        return if (configuration.useProjectSdk) {
            PythonExecuteUtils.executePyModuleScript(
                project,
                pythonSdk,
                moduleToRun,
                parameters,
                timeout = Duration.INFINITE
            )
        } else {
            val commandLine = GeneralCommandLine()
            commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            commandLine.withWorkingDirectory(configuration.projectDirectory?.let { Path.of(it) })
            commandLine.withExePath(configuration.executablePath)
            commandLine.withParameters(parameters)
            ExecUtil.execAndGetOutput(commandLine)
        }
    }
}