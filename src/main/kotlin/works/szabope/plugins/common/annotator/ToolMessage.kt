package works.szabope.plugins.common.annotator

interface ToolMessage {
    val message: String
    val line: Int
    val column: Int
}