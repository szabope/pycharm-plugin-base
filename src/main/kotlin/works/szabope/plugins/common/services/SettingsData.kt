package works.szabope.plugins.common.services

interface SettingsData : BasicSettingsData {
    val useProjectSdk: Boolean
    val workingDirectory: String?
    val excludeNonProjectFiles: Boolean
}