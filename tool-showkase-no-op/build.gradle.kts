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
    mustRunAfter(":tool-showkase-no-op:generateReleaseRFile")
}

dependencies {
    implementation(libs.kotlin.core)
    // Sentinel is provided by the app. Production variants use sentinel-no-op, and declaring the
    // real library as api here would drag it into those builds.
    compileOnly(libs.library)
    // Kotlin-only artifact, no Compose. Keeps the app's @ShowkaseRoot class resolvable in production.
    api(libs.showkase.annotation)
}

val groupId: String by project

mavenPublishing {
    coordinates(
        groupId = groupId,
        artifactId = "tool-showkase-no-op",
        version = releaseConfig["version"] as String
    )
}
