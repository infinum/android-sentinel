import kotlinx.validation.KotlinApiBuildTask
import kotlinx.validation.KotlinApiCompareTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompileTool

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

// Build configuration - centralized in config.gradle.kts
apply(from = "config.gradle.kts")


plugins {
    alias(libs.plugins.kotlin.binary.compatibility)
    alias(libs.plugins.gradle.maven.publish) apply false
}

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
    pluginManager.withPlugin("com.android.library") {
        configureApiValidation()
    }
}

// BCV only configures itself when the kotlin-android plugin is applied, which AGP built-in Kotlin replaces,
// so the apiBuild, apiCheck and apiDump tasks it would create for the release compilation are registered here
fun Project.configureApiValidation() {
    // The BCV plugin creates this configuration but adds kotlin-metadata-jvm only when it sees a Kotlin plugin
    dependencies {
        add("bcv-rt-jvm-cp", "org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}")
    }

    val apiDir = layout.projectDirectory.dir("api")

    val apiBuild = tasks.register<KotlinApiBuildTask>("apiBuild") {
        description = "Builds Kotlin API for the release compilation of ${project.name}"
        inputClassesDirs.from(
            tasks.named<KotlinCompileTool>("compileReleaseKotlin").flatMap { it.destinationDirectory },
            tasks.named<JavaCompile>("compileReleaseJavaWithJavac").flatMap { it.destinationDirectory }
        )
        outputApiFile.set(layout.buildDirectory.file("api/${project.name}.api"))
        runtimeClasspath.from(configurations.named("bcv-rt-jvm-cp-resolver"))
    }

    val apiCheck = tasks.register<KotlinApiCompareTask>("apiCheck") {
        group = "verification"
        description = "Checks signatures of public API against the golden value in API folder for ${project.name}"
        projectApiFile.set(apiDir.file("${project.name}.api"))
        generatedApiFile.set(apiBuild.flatMap { it.outputApiFile })
    }

    tasks.register<Copy>("apiDump") {
        group = "other"
        description = "Syncs the API file for ${project.name}"
        from(apiBuild.flatMap { it.outputApiFile })
        into(apiDir)
    }

    tasks.named("check") {
        dependsOn(apiCheck)
    }
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
    options.compilerArgs.addAll(listOf("--release", "11"))
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
        ":tool-googleplay:lintRelease",
        ":tool-showkase:lintRelease",
        ":tool-showkase-no-op:lintRelease",
        ":tool-timber:lintRelease",
        ":tool-networkemulator-okhttp:lintRelease",
        ":tool-networkemulator-okhttp-no-op:lintRelease"
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
        ":tool-googleplay:detekt",
        ":tool-showkase:detekt",
        ":tool-showkase-no-op:detekt",
        ":tool-timber:detekt",
        ":tool-networkemulator-okhttp:detekt",
        ":tool-networkemulator-okhttp-no-op:detekt"
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
