@file:Suppress("UnstableApiUsage")

package works.szabope.plugins.common.run


/**
 * Partially reimplementing Sh Plugin, because com.intellij.sh.run.ShRunConfigurationProfileState#createCommandLineForScript
 * turns console mode off, which results in stderr being redirected to stdout. The class is final.
 */

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.configurations.GeneralCommandLine.ParentEnvironmentType
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.NioFiles
import com.intellij.platform.eel.EelDescriptor
import com.intellij.platform.eel.EelPlatform
import com.intellij.platform.eel.provider.LocalEelDescriptor
import com.intellij.platform.eel.provider.asEelPath
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.toEelApiBlocking
import com.intellij.platform.eel.provider.utils.EelPathUtils.getNioPath
import com.intellij.platform.eel.provider.utils.fetchLoginShellEnvVariablesBlocking
import com.intellij.util.io.BaseOutputReader
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.function.Supplier

// from com.intellij.sh.run.ShConfigurationType
class ShConfigurationType : SimpleConfigurationType(
    "MyShConfigurationType", ShLanguage.INSTANCE.id, "TODO", //TODO
    NotNullLazyValue.lazy(Supplier { AllIcons.Nodes.Console })
) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val configuration = ShRunConfiguration(project, this)
        val defaultShell = getDefaultShell(project)
        configuration.interpreterPath = defaultShell
        val projectPath = project.basePath
        if (projectPath != null) {
            configuration.workingDirectory = projectPath
        }
        return configuration
    }

    companion object {
        @JvmStatic
        val INSTANCE = ShConfigurationType()

        fun getDefaultShell(project: Project): String {
            val eelDescriptor = if (project.isDefault) LocalEelDescriptor else project.getEelDescriptor()
            return if (eelDescriptor === LocalEelDescriptor) getDefaultShell() else trivialDefaultShellDetection(
                eelDescriptor
            )
        }

        private fun getDefaultShell(): String {
            if (SystemInfo.isWindows) {
                return "powershell.exe"
            } else {
                val shell = System.getenv("SHELL")
                val shellPath = if (shell != null) NioFiles.toPath(shell) else null
                if (shellPath != null && Files.exists(shellPath, *arrayOfNulls<LinkOption>(0))) {
                    return shell!!
                } else {
                    val bashPath = NioFiles.toPath("/bin/bash")
                    return if (bashPath != null && Files.exists(
                            bashPath, *arrayOfNulls<LinkOption>(0)
                        )
                    ) bashPath.toString() else "/bin/sh"
                }
            }
        }

        private fun trivialDefaultShellDetection(eelDescriptor: EelDescriptor): String {
            val eel = eelDescriptor.toEelApiBlocking()
            val shell = eel.exec.fetchLoginShellEnvVariablesBlocking()["SHELL"]
            if (shell != null) {
                val shellPath = getNioPath(shell, eelDescriptor)
                if (Files.isExecutable(shellPath)) {
                    return shellPath.toString()
                }
            }

            if (eel.platform is EelPlatform.Linux) {
                val bashPath = getNioPath("/bin/bash", eelDescriptor)
                return if (Files.exists(
                        bashPath, *arrayOfNulls<LinkOption>(0)
                    )
                ) bashPath.toString() else getNioPath("/bin/sh", eelDescriptor).toString()
            } else {
                return "powershell.exe"
            }
        }
    }
}

class ShRunConfiguration(project: Project, factory: ConfigurationFactory) :
    RunConfigurationBase<RunConfiguration>(project, factory, "TODO") {
    lateinit var interpreterPath: String
    lateinit var scriptText: String
    lateinit var workingDirectory: String

    override fun getState(executor: Executor, environment: ExecutionEnvironment) =
        ShRunConfigurationProfileState(environment.project, this)

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration?> {
        throw UnsupportedOperationException()
    }
}

// from com.intellij.sh.run.ShRunConfigurationProfileState
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
        val commandLine = this.createCommandLineForScript(eelDescriptor)
        val processHandler = createProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return DefaultExecutionResult(processHandler)
    }

    @Throws(ExecutionException::class)
    private fun createProcessHandler(commandLine: GeneralCommandLine): ProcessHandler {
        return object : KillableProcessHandler(commandLine) {
            override fun readerOptions(): BaseOutputReader.Options {
                return BaseOutputReader.Options.forTerminalPtyProcess()
            }
        }
    }

    private fun createCommandLineForScript(eelDescriptor: EelDescriptor): GeneralCommandLine {
        val commandLine = PtyCommandLine()
//        commandLine.withConsoleMode(false) // default true
        commandLine.withInitialColumns(120)
        commandLine.withParentEnvironmentType(ParentEnvironmentType.CONSOLE)
        commandLine.withWorkingDirectory(Path.of(myRunConfiguration.workingDirectory))
        commandLine.withExePath(convertPathUsingEel(ShConfigurationType.getDefaultShell(myProject), eelDescriptor))
        commandLine.withParameters("-c")
        commandLine.withParameters(myRunConfiguration.scriptText)
        return commandLine
    }

    private fun computeEelDescriptor(): EelDescriptor {
        var eelDescriptor: EelDescriptor? = null
        if (!myRunConfiguration.workingDirectory.isEmpty()) {
            eelDescriptor = nullizeIfLocal(Path.of(myRunConfiguration.workingDirectory).getEelDescriptor())
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

// from com.intellij.sh.ShLanguage
class ShLanguage : Language(
    "works.szabope.plugins.common.Shell Script", *arrayOf("application/x-bsh", "application/x-sh", "text/x-script.sh")
) {
    companion object {
        @JvmStatic
        val INSTANCE = ShLanguage()
    }
}
