package common.toolWindow

import works.szabope.plugins.common.services.SeverityConfig

interface TreeModelDataItem {
    val file: String
    val line: Int
    val column: Int
    val message: String
    val code: String
    val severity: SeverityConfig

    fun toRepresentation(): String
}