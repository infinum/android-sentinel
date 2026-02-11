@Suppress("UNCHECKED_CAST")
val buildConfig = extra["buildConfig"] as Map<String, Any>

@Suppress("UNCHECKED_CAST")
val releaseConfig = extra["releaseConfig"] as Map<String, Any>

plugins {
    id("com.android.application")
    id("kotlin-android")
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
    }

    kotlin {
        jvmToolchain(8)
    }
    
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
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
