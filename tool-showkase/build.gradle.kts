@Suppress("UNCHECKED_CAST")
val buildConfig = extra["buildConfig"] as Map<String, Any>
@Suppress("UNCHECKED_CAST")
val releaseConfig = extra["releaseConfig"] as Map<String, Any>

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.gradle.maven.publish)
}

android {
    buildFeatures {
        buildConfig = false
    }

    compileSdk = buildConfig["compileSdk"] as Int
    buildToolsVersion = buildConfig["buildTools"] as String

    defaultConfig {
        minSdk = buildConfig["minSdk"] as Int
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
        }
    }

    namespace = "com.infinum.sentinel.tool.showkase"
    resourcePrefix = "sentinel_"

    kotlin {
        jvmToolchain(11)

        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    "-Xexplicit-api=strict",
                    "-Xjvm-default=all"
                )
            )
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }

}

tasks.named("dokkaGenerate") {
    mustRunAfter(":tool-showkase:generateReleaseRFile")
}

dependencies {
    implementation(libs.kotlin.core)
    api(libs.library)
    // Kotlin-only artifact, no Compose. Lets the app's @ShowkaseRoot class resolve in every variant.
    api(libs.showkase.annotation)
    // The browser runtime carries the whole Compose stack, so it must never be contributed to the
    // app's dependency resolution. The app supplies it per variant instead.
    compileOnly(libs.showkase)
}

val groupId: String by project

mavenPublishing {
    coordinates(
        groupId = groupId,
        artifactId = "tool-showkase",
        version = releaseConfig["version"] as String
    )
}
