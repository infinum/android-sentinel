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

    namespace = "com.infinum.sentinel.tool.networkemulator"
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

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

tasks.named("dokkaJavadoc") {
    mustRunAfter(":tool-networkemulator-okhttp-no-op:generateReleaseRFile")
}

dependencies {
    implementation(libs.kotlin.core)
    // OkHttp is provided by the app using the network emulator
    compileOnly(libs.okhttp)
}

apply(from = "publish.gradle")
