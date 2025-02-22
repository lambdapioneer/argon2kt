import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.lambdapioneer.argon2kt.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lambdapioneer.argon2kt.app"

        minSdk = 21
        targetSdk = 35

        versionCode = 1
        versionName = "1.0"

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        create("release") {
            // Intentionally storing plaintext passwords as this is just a demo app
            // that needs to be tested in release mode to verify proguard rules
            storeFile = file("../dummy_release_keys.jks")
            storePassword = "argon2kt"
            keyAlias = "key"
            keyPassword = "argon2kt"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":lib"))
    // implementation("com.lambdapioneer.argon2kt:argon2kt:1.6.0")

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.multidex:multidex:2.0.1")
}
