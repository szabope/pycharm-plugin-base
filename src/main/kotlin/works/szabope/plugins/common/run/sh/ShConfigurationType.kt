package works.szabope.plugins.common.run.sh

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.NioFiles
import com.intellij.platform.eel.EelDescriptor
import com.intellij.platform.eel.EelPlatform
import com.intellij.platform.eel.provider.LocalEelDescriptor
import com.intellij.platform.eel.provider.getEelDescriptor
import com.intellij.platform.eel.provider.toEelApiBlocking
import com.intellij.platform.eel.provider.utils.EelPathUtils
import com.intellij.platform.eel.provider.utils.fetchLoginShellEnvVariablesBlocking
import works.szabope.plugins.common.CommonBundle
import java.nio.file.Files
import java.nio.file.LinkOption
import java.util.function.Supplier

class ShConfigurationType : SimpleConfigurationType(
    "works.szabope.plugins.common.run.sh.ShConfigurationType",
    ShLanguage.INSTANCE.id,
    CommonBundle.message("sh.run.configuration.description.0.configuration", ShLanguage.INSTANCE.id),
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
                val shellPath = EelPathUtils.getNioPath(shell, eelDescriptor)
                if (Files.isExecutable(shellPath)) {
                    return shellPath.toString()
                }
            }

            if (eel.platform is EelPlatform.Linux) {
                val bashPath = EelPathUtils.getNioPath("/bin/bash", eelDescriptor)
                return if (Files.exists(
                        bashPath, *arrayOfNulls<LinkOption>(0)
                    )
                ) bashPath.toString() else EelPathUtils.getNioPath("/bin/sh", eelDescriptor).toString()
            } else {
                return "powershell.exe"
            }
        }
    }
}