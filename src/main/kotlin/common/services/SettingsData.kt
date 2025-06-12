package common.services

interface SettingsData : BasicSettingsData {
    val useProjectSdk: Boolean
    val projectDirectory: String?
    val excludeNonProjectFiles: Boolean
}