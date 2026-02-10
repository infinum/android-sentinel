@Suppress("UNCHECKED_CAST")
val buildConfig = extra["buildConfig"] as Map<String, Any>

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
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

    namespace = "com.infinum.sentinel"
    resourcePrefix = "sentinel_"

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

    buildFeatures {
        viewBinding = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

tasks.named("dokkaJavadoc") {
    mustRunAfter(":sentinel-no-op:generateReleaseRFile")
}

dependencies {
    implementation(libs.kotlin.core)
    implementation(libs.androidx.startup)
}

apply(from = "publish.gradle")
