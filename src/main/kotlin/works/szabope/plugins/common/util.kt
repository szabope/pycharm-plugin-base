package works.szabope.plugins.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.projectRoots.Sdk
import com.jetbrains.python.sdk.findPythonSdk
import com.jetbrains.python.sdk.pythonSdk
import java.util.concurrent.CancellationException

// copied from com.intellij.grazie.utils.Utils
fun String.trimToNull(): String? = trim().takeIf(String::isNotBlank)

// Workaround: IntelliJ's BaseState.string() delegate normalizes "" to null on persist/load.
// Store a single space instead of blank so the value survives serialization.
fun String.blankToSingleSpace(): String = ifBlank { " " }

// copied from com.intellij.collaboration.util.resultUtil
inline fun <T> Result<T>.processErrorAndGet(handler: (e: Throwable) -> Unit): T =
    onFailure {
        if (it !is CancellationException && it !is Error) handler(it)
    }.getOrThrow()

// restricting plugin functionality to a single SDK in the project, since multi-(IJ) module configuration would require
// a serious redesign, and frankly, I doubt that it is a typical use case.
/**
 * Resolve a single SDK configured for any IJ modules.
 * @return null if multiple distinct SDKs are configured for multiple IJ modules
 */
suspend fun Project.resolveModulePythonSdk(): Sdk? {
    return modules.mapNotNull { it.findPythonSdk() }.distinctBy { it.homePath }.singleOrNull()
}

/**
 * Resolve a single SDK configured for any IJ modules.
 * @return null if multiple distinct SDKs are configured for multiple IJ modules
 */
fun Project.resolveModulePythonSdkNow(): Sdk? {
    return modules.mapNotNull { it.pythonSdk }.distinctBy { it.homePath }.singleOrNull()
}

/**
 * Count distinct SDKs configured for all IJ modules.
 * @return number of SDKs that are configured for IJ modules
 */
fun Project.countPythonSdkModulesNow(): Int {
    return modules.mapNotNull { it.pythonSdk }.distinctBy { it.homePath }.count()
}
