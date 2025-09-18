package works.szabope.plugins.common.run.sh

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ProgramRunner
import com.intellij.openapi.project.Project
import com.intellij.platform.eel.EelDescriptor
import com.intellij.platform.eel.provider.LocalEelDescriptor
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.util.io.BaseOutputReader
import java.nio.file.Path

class ShRunConfigurationProfileState(
    private val myProject: Project, private val myRunConfiguration: ShRunConfiguration
) : RunProfileState {

    @Throws(ExecutionException::class)
    override fun execute(executor: Executor?, runner: ProgramRunner<*>): ExecutionResult {
        val eelDescriptor = this.computeEelDescriptor()
        return this.buildExecutionResult(eelDescriptor)
    }

    @Throws(ExecutionException::class)
    private fun buildExecutionResult(eelDescriptor: EelDescriptor): ExecutionResult {
        val commandLine = createCommandLineForScript(eelDescriptor)
        val processHandler = createProcessHandler(commandLine)
        return DefaultExecutionResult(processHandler)
    }

    @Throws(ExecutionException::class)
    private fun createProcessHandler(commandLine: GeneralCommandLine): ProcessHandler {
        return object : KillableProcessHandler(commandLine) {
            override fun readerOptions(): BaseOutputReader.Options {
                return BaseOutputReader.Options.BLOCKING
            }
        }
    }

    private fun createCommandLineForScript(eelDescriptor: EelDescriptor): GeneralCommandLine {
        val commandLine = GeneralCommandLine()
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
        commandLine.withWorkingDirectory(myRunConfiguration.workingDirectory?.let { Path.of(it) })
        commandLine.withExePath(convertPathUsingEel(myRunConfiguration.command, eelDescriptor))
        commandLine.withParameters(myRunConfiguration.parameters)
        return commandLine
    }

    private fun computeEelDescriptor(): EelDescriptor {
        var eelDescriptor: EelDescriptor? = null
        val workingDirectory = myRunConfiguration.workingDirectory ?: ""
        if (!workingDirectory.isEmpty()) {
            eelDescriptor = nullizeIfLocal(Path.of(workingDirectory).getEelDescriptor())
        }

        if (eelDescriptor == null && !myRunConfiguration.interpreterPath.isEmpty()) {
            eelDescriptor = nullizeIfLocal(Path.of(myRunConfiguration.interpreterPath).getEelDescriptor())
        }

        return eelDescriptor ?: myProject.getEelDescriptor()
    }

    private fun nullizeIfLocal(eelDescriptor: EelDescriptor): EelDescriptor? {
        return if (eelDescriptor === LocalEelDescriptor) null else eelDescriptor
    }

    private fun convertPathUsingEel(path: String, eelDescriptor: EelDescriptor): String {
        return if (path.isEmpty()) {
            path
        } else {
            if (eelDescriptor === LocalEelDescriptor) path else Path.of(path).asEelPath().toString()
        }
    }
}