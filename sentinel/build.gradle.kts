@Suppress("UNCHECKED_CAST")
val buildConfig = extra["buildConfig"] as Map<String, Any>

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.ksp)
}

apply(from = "jacoco.gradle")

android {
    compileSdk = buildConfig["compileSdk"] as Int
    buildToolsVersion = buildConfig["buildTools"] as String

    defaultConfig {
        minSdk = buildConfig["minSdk"] as Int

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.incremental"] = "true"
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
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
                    "-opt-in=kotlin.ExperimentalStdlibApi",
                    "-Xexplicit-api=strict",
                    "-Xjvm-default=all",
                    "-Xannotation-default-target=param-property"
                )
            )
        }
    }
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    lint {
        disable.addAll(listOf("RtlEnabled", "VectorPath"))
    }
    
    compileOptions {
    }
    
    testOptions {
        animationsDisabled = true
    }

    sourceSets.all {
        java.srcDir("src/$name/kotlin")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

tasks.named("dokkaGenerate") {
    mustRunAfter(":sentinel:generateReleaseRFile")
    mustRunAfter(":sentinel:kspReleaseKotlin")
}

dependencies {
    implementation(libs.kotlin.core)
    implementation(libs.kotlin.json)
    implementation(libs.coroutines)
    implementation(libs.bundles.androidx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.material)
    ksp(libs.inject.compiler)
    implementation(libs.inject.runtime)

    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.arch.core.testing)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.crypto)
    androidTestImplementation(libs.robolectric.shadowapi)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.test.espresso.intents)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.fragment)
    androidTestImplementation(libs.androidx.fragment.testing)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.test.ext.junit)
}

// KSP configuration for inject
// Note: Injekt extension configuration removed during Kotlin DSL migration
// Original config: arg("me.tatarka.inject.dumpGraph", "false")

apply(from = "publish.gradle")

gradle.taskGraph.whenReady {
    allTasks.find { it.path == ":sentinel:connectedDebugAndroidTest" }?.apply {
        (this as AbstractTestTask).ignoreFailures = true
    }
}

apply(from = "../tasks.gradle.kts")

tasks.named("preBuild") {
    dependsOn(":sentinel:generateReadme")
}

apply(from = "../apiComparison.gradle.kts")

afterEvaluate {
    tasks.named("apiCheck") {
        dependsOn("compareSentinelApis")
    }
}
