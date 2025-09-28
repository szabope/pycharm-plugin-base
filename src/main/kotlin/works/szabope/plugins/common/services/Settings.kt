package works.szabope.plugins.common.services

interface Settings : SettingsData {
    var isAutoScrollToSource: Boolean
    override var scanBeforeCheckIn: Boolean
    override var excludeNonProjectFiles: Boolean
    override var projectDirectory: String?
    override var useProjectSdk: Boolean
    override var arguments: String
    override var configFilePath: String
    override var executablePath: String

    suspend fun initSettings(oldSettings: BasicSettingsData)
    fun getData(): ImmutableSettingsData
}