package works.szabope.plugins.common.run

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.execution.process.ScriptRunnerUtil.STDERR_OUTPUT_KEY_FILTER
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.delete
import kotlin.io.path.writeText

class ShellExecutionTest : BasePlatformTestCase() {
    //todo: "StdErr" why empty?
//    works.szabope.plugins.common.run.ProcessException: Exit code: 1
//    StdErr:
    fun `test execute command in shell`() {
        val environment = CliExecutionEnvironmentFactory(project).createEnvironment(">&2 echo 8")
        ProgramRunnerUtil.executeConfigurationAsync(environment, false, false) { descriptor ->
            val processHandler = requireNotNull(descriptor.processHandler)
            processHandler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    thisLogger().info("Process event: $event outputType: $outputType")
                }
            })
        }
        PlatformTestUtil.waitWhileBusy { true }
//        assertEquals("8", stdOut)
    }

    //TODO: test on windows
    fun `test script runner util`() {
        val tempFile = kotlin.io.path.createTempFile(prefix = "script_runner_", suffix = ".sh")
        try {
            tempFile.toFile().deleteOnExit()
            tempFile.writeText(">&2 echo 8")
            val tempVirtualFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(tempFile)
            val handler = ScriptRunnerUtil.execute("/bin/bash", null, tempVirtualFile, emptyArray<String>())
            val out = ScriptRunnerUtil.getProcessOutput(handler, STDERR_OUTPUT_KEY_FILTER, 1000)
            assertEquals("8\n", out)
        } finally {
            tempFile.delete()
        }
    }
}