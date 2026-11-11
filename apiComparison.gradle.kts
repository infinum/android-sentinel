tasks.register("compareSentinelApis") {
    val sentinelApi = file("${project.rootDir}/sentinel/api/sentinel.api")
    val noOpApi = file("${project.rootDir}/sentinel-no-op/api/sentinel-no-op.api")

    doFirst {
        if (!sentinelApi.exists() || !noOpApi.exists()) {
            throw GradleException("API dump files not found. Run `apiDump` task first.")
        }

        val targetBlocks = listOf(
            "public final class com/infinum/sentinel/Sentinel {",
            "public final class com/infinum/sentinel/SentinelInitializer {",
            "public abstract interface class com/infinum/sentinel/Sentinel\$Tool {"
        )

        fun extractRelevantLines(apiFile: File): Set<String> {
            val relevantLines = mutableSetOf<String>()
            var insideTargetBlock = false

            apiFile.forEachLine { line ->
                val trimmed = line.trim()

                if (targetBlocks.contains(trimmed)) {
                    insideTargetBlock = true
                }

                if (insideTargetBlock) {
                    relevantLines.add(line)
                }

                if (trimmed.isEmpty()) {
                    insideTargetBlock = false
                }
            }
            return relevantLines
        }

        val sentinelRelevant = extractRelevantLines(sentinelApi)
        val noOpRelevant = extractRelevantLines(noOpApi)

        val missingLines = sentinelRelevant - noOpRelevant

        if (missingLines.isNotEmpty()) {
            println("🚨 API mismatch detected! The following APIs in `Sentinel` are missing in `Sentinel no-op`:")
            missingLines.forEach { println("❌ $it") }
            throw GradleException("API mismatch detected between Sentinel and Sentinel no-op!")
        } else {
            println("✅ API is identical between Sentinel and Sentinel no-op!")
        }
    }
}
