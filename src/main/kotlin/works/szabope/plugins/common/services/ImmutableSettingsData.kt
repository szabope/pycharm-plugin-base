package works.szabope.plugins.common.services

interface ImmutableSettingsData : SettingsData {
    override val executablePath: String
    override val useProjectSdk: Boolean
    override val configFilePath: String
    override val arguments: String
    override val workingDirectory: String
    override val excludeNonProjectFiles: Boolean
    override val scanBeforeCheckIn: Boolean
}