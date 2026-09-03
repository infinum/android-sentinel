import java.util.Properties

@Suppress("UNCHECKED_CAST")
val buildConfig = extra["buildConfig"] as Map<String, Any>

@Suppress("UNCHECKED_CAST")
val releaseConfig = extra["releaseConfig"] as Map<String, Any>

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = buildConfig["compileSdk"] as Int
    buildToolsVersion = buildConfig["buildTools"] as String

    defaultConfig {
        applicationId = releaseConfig["group"] as String
        applicationIdSuffix = ".sample"
        minSdk = buildConfig["minSdk"] as Int
        targetSdk = buildConfig["targetSdk"] as Int
        versionCode = releaseConfig["versionCode"] as Int
        versionName = releaseConfig["version"] as String

        val localProperties =
            Properties().apply {
                val file = rootProject.file("local.properties")
                if (file.exists()) file.inputStream().use { load(it) }
            }
        buildConfigField(
            "String",
            "CLAUDE_CODE_TOKEN",
            "\"${localProperties.getProperty("claude.code.token", "")}\"",
        )
        buildConfigField(
            "String",
            "CLAUDE_ROUTINE_FIRE_URL",
            "\"${localProperties.getProperty("claude.routine.fireUrl", "")}\"",
        )
    }

    signingConfigs {
        create("dummy") {
            storeFile = file("dummy.jks")
            storePassword = "dummydummy"
            keyAlias = "dummy"
            keyPassword = "dummydummy"
        }
    }

    buildTypes {
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("dummy")
        }
    }

    namespace = "com.infinum.sentinel.sample"

    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true
    }

    kotlin {
        jvmToolchain(11)
    }
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(11))
        }
    }

}

dependencies {
    implementation(libs.kotlin.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.crypto)
    implementation(libs.material)
    implementation(libs.timber)
    implementation(libs.coroutines)

    // OkHttp for testing network emulator
    implementation(libs.okhttp)

    // Compose screen used to exercise Sentinel's Compose annotation-target resolution.
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    // ui-tooling is what puts ui-tooling-data on the runtime classpath, which is what lets
    // Sentinel read composable source locations. Without it annotations still work, but with
    // no file:line.
    debugImplementation(libs.compose.ui.tooling)

//    debugImplementation(libs.library)
//    releaseImplementation(libs.librarynoop)

    debugImplementation(project(":sentinel"))
    releaseImplementation(project(":sentinel-no-op"))

//    debugImplementation(libs.bundles.tools)
//    releaseImplementation(libs.toolnetworkemulatorokhttpnoop)

    debugImplementation(project(":tool-appgallery"))
    debugImplementation(project(":tool-chucker"))
    debugImplementation(project(":tool-collar"))
    debugImplementation(project(":tool-dbinspector"))
    debugImplementation(project(":tool-googleplay"))
    debugImplementation(project(":tool-leakcanary"))
    debugImplementation(project(":tool-networkemulator-okhttp"))
    releaseImplementation(project(":tool-networkemulator-okhttp-no-op"))
    debugImplementation(project(":tool-timber"))
}
