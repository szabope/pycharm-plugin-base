package works.szabope.plugins.common

// copied from com.intellij.grazie.utils.Utils
fun String.trimToNull(): String? = trim().takeIf(String::isNotBlank)