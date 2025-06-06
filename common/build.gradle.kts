plugins {
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intelliJPlatform) // IntelliJ Platform Gradle Plugin
}

group = "works.szabope.plugins"
version = "0.0.1"

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(
            providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"), useInstaller = true
        )

        // Plugin Dependencies. Uses `platformBundledPlugins` property from the gradle.properties file for bundled IntelliJ Platform plugins.
        bundledPlugins(providers.gradleProperty("platformBundledPlugins").map { it.split(',') })

        // Plugin Dependencies. Uses `platformPlugins` property from the gradle.properties file for plugin from JetBrains Marketplace.
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
    }
}

kotlin {
    jvmToolchain(21)
}

intellijPlatform {
    // buildSearchableOptions task must currently be disabled explicitly to work around Only one instance of IDEA can be run at a time problem.
    // https://plugins.jetbrains.com/docs/intellij/ide-development-instance.html#intellij-platform-gradle-plugin-2x
    buildSearchableOptions = false
}

intellijPlatformTesting {
}
