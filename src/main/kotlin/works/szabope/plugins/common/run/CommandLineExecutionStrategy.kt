package works.szabope.plugins.common.run

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import java.nio.file.Path

fun commandLineProcessHandler(
    executablePath: String, workingDirectory: String?, parameters: List<String> = emptyList()
): OSProcessHandler {
    val commandLine = GeneralCommandLine().apply {
        withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        withWorkingDirectory(workingDirectory?.let { Path.of(it) })
        withExePath(executablePath)
        withParameters(parameters)
    }
    return ToolProcessHandler(commandLine)
}