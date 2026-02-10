buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.tools.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.kotlin.serialization)
        classpath(libs.detekt.gradle)
        classpath(libs.dokka)
    }
}

// Build configuration - must be defined early for all projects
allprojects {
    val major = 1
    val minor = 5
    val patch = 1

    extra["buildConfig"] = mapOf(
        "minSdk" to 24,
        "compileSdk" to 36,
        "targetSdk" to 36,
        "buildTools" to "35.0.0"
    )

    extra["releaseConfig"] = mapOf(
        "group" to "com.infinum.sentinel",
        "version" to "$major.$minor.$patch",
        "versionCode" to (major * 100 * 100 + minor * 100 + patch)
    )
}

// Maven/Sonatype configuration
apply(from = "maven.gradle.kts")

plugins {
    alias(libs.plugins.kotlin.binary.compatibility)
}

// Note: apiValidation configuration should be here
// For now, commented out to allow build to proceed
// This will need to be configured properly after successful migration
// apiValidation {
//     ignoredProjects.add("sample")
// }

allprojects {

    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    apply(from = "$rootDir/detekt.gradle")
    apply(from = "$rootDir/dokka.gradle")
}

gradle.projectsEvaluated {
    subprojects.forEach { subproject ->
        val apiCheckTask = subproject.tasks.findByName("apiCheck")
        if (apiCheckTask != null) {
            subproject.tasks.matching { it.name.startsWith("publish") }.configureEach {
                dependsOn(apiCheckTask)
            }
        }
    }
}

apply(from = "deploy.gradle.kts")

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("--release", "8"))
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

tasks.register("lintAll") {
    dependsOn(
        ":sentinel:lintRelease",
        ":sentinel-no-op:lintRelease",
        ":tool-chucker:lintRelease",
        ":tool-collar:lintRelease",
        ":tool-dbinspector:lintRelease",
        ":tool-leakcanary:lintRelease",
        ":tool-appgallery:lintRelease",
        ":tool-googleplay:lintRelease"
    )
    group = "Verification"
    description = "Run Detekt on all modules"
}

tasks.register("detektAll") {
    dependsOn(
        ":sentinel:detekt",
        ":sentinel-no-op:detekt",
        ":tool-chucker:detekt",
        ":tool-collar:detekt",
        ":tool-dbinspector:detekt",
        ":tool-leakcanary:detekt",
        ":tool-appgallery:detekt",
        ":tool-googleplay:detekt"
    )
    group = "Verification"
    description = "Run Detekt on all modules"
}

tasks.register("runStaticChecks") {
    dependsOn(
        ":lintAll",
        ":detektAll"
    )
    group = "Verification"
    description = "Run static checks on all modules"
}
