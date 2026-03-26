package works.szabope.plugins.common.toolWindow

import works.szabope.plugins.common.services.SeverityConfig

data class DefaultTreeModelDataItem(
    override val file: String,
    override val line: Int,
    override val column: Int,
    override val message: String,
    override val code: String,
    override val severity: SeverityConfig,
) : TreeModelDataItem {
    override fun toRepresentation() = "[$code] $message"
}