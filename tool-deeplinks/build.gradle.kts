@Suppress("UNCHECKED_CAST")
val buildConfig = extra["buildConfig"] as Map<String, Any>
@Suppress("UNCHECKED_CAST")
val releaseConfig = extra["releaseConfig"] as Map<String, Any>

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.gradle.maven.publish)
}

android {
    compileSdk = buildConfig["compileSdk"] as Int
    buildToolsVersion = buildConfig["buildTools"] as String

    defaultConfig {
        minSdk = buildConfig["minSdk"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    namespace = "com.infinum.sentinel.tool.deeplinks"
    resourcePrefix = "sentinel_"

    kotlin {
        jvmToolchain(11)

        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlin.ExperimentalStdlibApi",
                    "-Xexplicit-api=strict",
                    "-jvm-default=no-compatibility"
                )
            )
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }

    lint {
        disable.addAll(listOf("RtlEnabled", "VectorPath"))
    }

    testOptions {
        animationsDisabled = true
    }

    buildFeatures {
        buildConfig = false
        compose = true
    }
}

tasks.named("dokkaGenerate") {
    mustRunAfter(":tool-deeplinks:generateReleaseRFile")
}

dependencies {
    api(libs.library)
    implementation(libs.kotlin.core)
    implementation(libs.kotlin.json)
    implementation(libs.coroutines)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.tooling)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.robolectric.shadowapi)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)

    testImplementation(libs.junit)
}

val groupId = project.property("groupId") as String

mavenPublishing {
    coordinates(
        groupId = groupId,
        artifactId = "tool-deeplinks",
        version = releaseConfig["version"] as String
    )
}
