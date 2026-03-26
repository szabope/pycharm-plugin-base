package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import java.nio.file.Path

fun commandLineProcessHandler(
    executablePath: String, workingDirectory: String?, parameters: List<String> = emptyList()
): OSProcessHandler {
    val commandLine = GeneralCommandLine()
    commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
    commandLine.withWorkingDirectory(workingDirectory?.let { Path.of(it) })
    commandLine.withExePath(executablePath)
    commandLine.withParameters(parameters)
    return ToolProcessHandler(commandLine)
}