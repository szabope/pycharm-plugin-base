package works.szabope.plugins.common.test

import com.intellij.openapi.application.runWriteActionAndWait
import com.intellij.openapi.progress.withCurrentThreadCoroutineScopeBlocking
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.ThrowableRunnable
import com.jetbrains.python.sdk.pythonSdk
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService
import works.szabope.plugins.common.test.sdk.PythonMockSdk

abstract class AbstractPluginTestCase : BasePlatformTestCase() {

    // local variables are not supported in mockk answer, yet
    private lateinit var packageManagementServiceStub: AbstractPluginPackageManagementService

    override fun setUp() {
        // FIXME: this is a duct tape for
        //  com.intellij.python.community.services.systemPython.searchPythonsPhysicallyNoCache
        //  accessing /usr/bin/python3(\.\d+)? which is not allowed from tests
        VfsRootAccess.allowRootAccess(testRootDisposable, "/usr/bin")
        setupPackageManagementServiceMock { p ->
            if (!::packageManagementServiceStub.isInitialized) {
                packageManagementServiceStub = createPackageManagementServiceStub(p)
            }
            packageManagementServiceStub
        }
        super.setUp()
        onSetUp()
    }

    protected abstract fun setupPackageManagementServiceMock(
        stubProvider: (Project) -> AbstractPluginPackageManagementService
    )

    protected abstract fun createPackageManagementServiceStub(project: Project): AbstractPluginPackageManagementService

    protected open fun onSetUp() {}

    override fun runTestRunnable(testRunnable: ThrowableRunnable<Throwable>) {
        withCurrentThreadCoroutineScopeBlocking { super.runTestRunnable(testRunnable) }
    }

    override fun tearDown() {
        clearAllMocks()
        unmockkAll()
        super.tearDown()
    }

    /**
     * https://youtrack.jetbrains.com/issue/IJPL-197007
     */
    override fun getProjectDescriptor(): LightProjectDescriptor? = LightProjectDescriptor()

    fun withMockSdk(path: String, action: (Sdk) -> Unit) {
        val mockSdk = PythonMockSdk.create(path)
        runWriteActionAndWait {
            ProjectJdkTable.getInstance().addJdk(mockSdk)
        }
        project.pythonSdk = null // does nothing beyond reminding me not to count on it, e.g. uv
        module.pythonSdk = mockSdk
        try {
            action(mockSdk)
        } finally {
            module.pythonSdk = null
            runWriteActionAndWait {
                ProjectJdkTable.getInstance().removeJdk(mockSdk)
            }
        }
    }
}
