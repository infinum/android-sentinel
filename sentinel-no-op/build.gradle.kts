@Suppress("UNCHECKED_CAST")
val buildConfig = extra["buildConfig"] as Map<String, Any>
@Suppress("UNCHECKED_CAST")
val releaseConfig = extra["releaseConfig"] as Map<String, Any>

plugins {
    id("com.android.library")
    id("kotlin-android")
    alias(libs.plugins.gradle.maven.publish)
}

android {
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

    namespace = "com.infinum.sentinel"
    resourcePrefix = "sentinel_"

    kotlin {
        jvmToolchain(8)
        
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
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    sourceSets.all {
        java.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        viewBinding = true
    }
}

tasks.named("dokkaGenerate") {
    mustRunAfter(":sentinel-no-op:generateReleaseRFile")
}

dependencies {
    implementation(libs.kotlin.core)
    implementation(libs.androidx.startup)
}

val groupId: String by project

mavenPublishing {
    coordinates(
        groupId = groupId,
        artifactId = "sentinel-no-op",
        version = releaseConfig["version"] as String
    )
}
