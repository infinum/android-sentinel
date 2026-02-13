// Centralized build configuration for all projects
allprojects {
    val major = 2
    val minor = 0
    val patch = 0

    extra["buildConfig"] = mapOf(
        "minSdk" to 24,
        "compileSdk" to 36,
        "targetSdk" to 36,
        "buildTools" to "36.0.0"
    )

    extra["releaseConfig"] = mapOf(
        "group" to "com.infinum.sentinel",
        "version" to "$major.$minor.$patch",
        "versionCode" to (major * 100 * 100 + minor * 100 + patch)
    )
}
