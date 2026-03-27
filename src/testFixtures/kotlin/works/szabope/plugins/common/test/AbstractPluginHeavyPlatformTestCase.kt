package works.szabope.plugins.common.test

import com.intellij.openapi.progress.withCurrentThreadCoroutineScopeBlocking
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess
import com.intellij.testFramework.HeavyPlatformTestCase
import com.intellij.util.ThrowableRunnable
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import works.szabope.plugins.common.services.AbstractPluginPackageManagementService

abstract class AbstractPluginHeavyPlatformTestCase : HeavyPlatformTestCase() {

    // local variables are not supported in mockk answer, yet
    private lateinit var packageManagementServiceStub: AbstractPluginPackageManagementService

    override fun setUp() {
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
}
