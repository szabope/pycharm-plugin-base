plugins {
    id("java") // Java support
    id("org.gradle.kotlin.kotlin-dsl") version "5.2.0"
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.kover) // Gradle Kover Plugin
    alias(libs.plugins.intelliJPlatform) apply false // IntelliJ Platform Gradle Plugin
    alias(libs.plugins.serialization) apply false // Gradle Kotlin Serialization Plugin
}

allprojects {
    plugins.apply("java")
    plugins.apply("org.jetbrains.kotlin.jvm")
}

subprojects {
    plugins.apply("org.jetbrains.intellij.platform")
}

group = providers.gradleProperty("pluginGroup").get()
//version = providers.gradleProperty("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

tasks.withType<Wrapper>(Wrapper::class.java).configureEach {
    distributionType = Wrapper.DistributionType.BIN
    gradleVersion = providers.gradleProperty("gradleVersion").get()
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}
