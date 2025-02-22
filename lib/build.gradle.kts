@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("signing")
}

android {
    namespace = "com.lambdapioneer.argon2kt"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        testOptions {
            targetSdk = 35
        }
        lint {
            targetSdk = 35
        }

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndkVersion = "27.2.12479018"

        ndk {
            abiFilters.addAll(listOf("x86", "armeabi-v7a", "x86_64", "arm64-v8a"))
        }

        externalNativeBuild {
            cmake {
                arguments.add("-DANDROID_TOOLCHAIN=clang")
                targets("argon2jni")
            }
        }

        setProperty("archivesBaseName", "argon2kt")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    externalNativeBuild {
        cmake {
            path = file("./CMakeLists.txt")
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

fun getProperty(name: String) = project.extra[name] as String

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = getProperty("GROUP")
            artifactId = getProperty("POM_ARTIFACT_ID")
            version = getProperty("VERSION_NAME")

            pom {
                name.set(getProperty("POM_NAME"))
                description.set(getProperty("POM_DESCRIPTION"))
                url.set(getProperty("POM_URL"))
                licenses {
                    license {
                        name.set(getProperty("POM_LICENCE_NAME"))
                        url.set(getProperty("POM_LICENCE_URL"))
                    }
                }
                developers {
                    developer {
                        id.set(getProperty("POM_DEVELOPER_ID"))
                        name.set(getProperty("POM_DEVELOPER_NAME"))
                        email.set(getProperty("POM_DEVELOPER_URL"))
                    }
                }
                scm {
                    connection.set(getProperty("POM_SCM_CONNECTION"))
                    developerConnection.set(getProperty("POM_SCM_DEV_CONNECTION"))
                    url.set(getProperty("POM_SCM_URL"))
                }
            }

            afterEvaluate {
                from(components["release"])
            }
        }
    }
    repositories {
        maven {
            name = "local"
            url = uri("${project.layout.buildDirectory}/repo")
        }
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: ""
                password = project.findProperty("ossrhPassword") as String? ?: ""
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["release"])
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.15.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("org.robolectric:robolectric:4.14.1")

    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("com.google.truth:truth:1.4.4")
}
