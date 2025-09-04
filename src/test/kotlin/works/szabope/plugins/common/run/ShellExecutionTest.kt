package works.szabope.plugins.common.run

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.execution.process.ScriptRunnerUtil.STDERR_OUTPUT_KEY_FILTER
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.Key
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.io.delete
import okio.Path.Companion.toPath
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.pathString
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
        val tempFile = kotlin.io.path.createTempFile(
            directory = project.basePath!!.toPath(true).toNioPath(),
            prefix = "script_runner_",
            suffix = ".sh",
            PosixFilePermissions.asFileAttribute(
                setOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE
                )
            )
        )
        try {
            tempFile.toFile().deleteOnExit()
            tempFile.writeText(">&2 echo 8")
//            val tempVirtualFile = VirtualFileManager.getInstance().refreshAndFindFileByNioPath(tempFile)
            val handler = ScriptRunnerUtil.execute(tempFile.pathString, null, null, emptyArray<String>())
            val out = ScriptRunnerUtil.getProcessOutput(handler, STDERR_OUTPUT_KEY_FILTER, 1000)
            assertEquals("8\n", out)
        } finally {
            tempFile.delete()
        }
    }
}