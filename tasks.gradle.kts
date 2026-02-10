// Use release config from root project
@Suppress("UNCHECKED_CAST")
val releaseConfig = rootProject.extra["releaseConfig"] as Map<String, Any>

fun replaceVersionsInFile(file: File) {
    var content = file.readText()
    content = content.replace(Regex("""sentinelVersion\s*=\s*".*""""), "sentinelVersion = \"${releaseConfig["version"]}\"")
    file.writeText(content)
}

tasks.register("generateReadme") {
    doFirst {
        replaceVersionsInFile(File("${rootDir}/README.md"))
    }
}
