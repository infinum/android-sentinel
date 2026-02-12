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
    buildFeatures {
        buildConfig = false
        viewBinding = true
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
}

tasks.named("dokkaGenerate") {
    mustRunAfter(":tool-networkemulator-okhttp:generateReleaseRFile")
}

dependencies {
    implementation(libs.kotlin.core)
    implementation(libs.coroutines)
    api(libs.library)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.recycler)
    implementation(libs.androidx.dynamicanimation)
    implementation(libs.material)
    // OkHttp is provided by the app using the network emulator
    compileOnly(libs.okhttp)
}

val groupId: String by project

mavenPublishing {
    coordinates(
        groupId = groupId,
        artifactId = "tool-networkemulator-okhttp",
        version = releaseConfig["version"] as String
    )
}
