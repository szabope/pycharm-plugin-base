package works.szabope.plugins.common

import java.util.concurrent.CancellationException

// copied from com.intellij.grazie.utils.Utils
fun String.trimToNull(): String? = trim().takeIf(String::isNotBlank)

// Workaround: IntelliJ's BaseState.string() delegate normalises "" to null on persist/load.
// Store a single space instead of blank so the value survives serialisation.
fun String.blankToSingleSpace(): String = ifBlank { " " }

// copied from com.intellij.collaboration.util.resultUtil
inline fun <T> Result<T>.processErrorAndGet(handler: (e: Throwable) -> Unit): T =
    onFailure {
        if (it !is CancellationException && it !is Error) handler(it)
    }.getOrThrow()
