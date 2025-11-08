package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import works.szabope.plugins.common.services.ImmutableSettingsData
import java.nio.file.Path

class CommandLineExecutionStrategy(
    configuration: ImmutableSettingsData, parameters: List<String> = emptyList()
) : ToolExecutionStrategy {

    override val processHandler: OSProcessHandler

    init {
        val commandLine = GeneralCommandLine()
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        commandLine.withWorkingDirectory(configuration.projectDirectory?.let { Path.of(it) })
        commandLine.withExePath(configuration.executablePath)
        commandLine.withParameters(parameters)
        processHandler = OSProcessHandler(commandLine)
    }
}