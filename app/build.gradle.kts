fun getSdkVersion(version: Provider<String>) = version.get().toInt()

fun getVersion(version: Provider<String>) = version.get()

plugins {
    id("com.android.application")
}

android {
    namespace = "com.lambdapioneer.argon2kt.app"
    compileSdk = getSdkVersion(libs.versions.compileSdk)
    ndkVersion = getVersion(libs.versions.ndk)

    defaultConfig {
        applicationId = "com.lambdapioneer.argon2kt.app"

        minSdk = getSdkVersion(libs.versions.minSdk)
        targetSdk = getSdkVersion(libs.versions.targetSdk)

        versionCode = 1
        versionName = "1.0"

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
            isShrinkResources = true
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

}

dependencies {
    implementation(project(":lib"))
    // implementation("com.lambdapioneer.argon2kt:argon2kt:1.6.0")

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
}
