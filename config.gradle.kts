// Centralized build configuration for all projects
allprojects {
    val major = 1
    val minor = 5
    val patch = 1

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
