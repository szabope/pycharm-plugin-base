package works.szabope.plugins.common.validator

data class ToolValidatorMessages(
    val pathNotExists: String,
    val pathIsDirectory: String,
    val pathNotExecutable: String,
    val unknownVersion: String,
    val invalidVersion: String,
    val notInstalled: String
)