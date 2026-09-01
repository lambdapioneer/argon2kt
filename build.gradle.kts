buildscript {
    dependencies {
        // Use a newer Kotlin compiler than the one bundled with AGP's built-in Kotlin support
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.androidApplication) apply false
}
