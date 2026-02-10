@Suppress("UNCHECKED_CAST")
val buildConfig = extra["buildConfig"] as Map<String, Any>

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    buildFeatures {
        buildConfig = false
    }

    compileSdk = buildConfig["compileSdk"] as Int
    buildToolsVersion = buildConfig["buildTools"] as String

    defaultConfig {
        minSdk = buildConfig["minSdk"] as Int
        targetSdk = buildConfig["targetSdk"] as Int
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.txt")
        }
    }

    namespace = "com.infinum.sentinel.tool.leakcanary"

    kotlin {
        jvmToolchain(8)
    }
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xexplicit-api=strict",
            "-Xjvm-default=all"
        )
    }

    sourceSets.all {
        java.srcDir("src/$name/kotlin")
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

tasks.named("dokkaJavadoc") {
    mustRunAfter(":tool-leakcanary:generateReleaseRFile")
}

dependencies {
    implementation(libs.kotlin.core)
    api(libs.library)
    api(libs.leakcanary)
}

apply(from = "publish.gradle")
