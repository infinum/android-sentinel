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

        // Tool wrappers, keyed by class header, e.g. "public final class com/infinum/sentinel/ui/tools/ChuckerTool : ... {"
        val toolClassPrefix = "com/infinum/sentinel/ui/tools/"

        fun extractToolBlocks(apiFile: File): Map<String, Set<String>> {
            val toolBlocks = mutableMapOf<String, MutableSet<String>>()
            var currentTool: String? = null

            apiFile.forEachLine { line ->
                val trimmed = line.trim()

                if (trimmed.endsWith("{") && trimmed.contains(" class $toolClassPrefix")) {
                    currentTool = trimmed.substringAfter(toolClassPrefix).substringBefore(" ")
                }

                currentTool?.let { toolBlocks.getOrPut(it) { mutableSetOf() }.add(line) }

                if (trimmed.isEmpty()) {
                    currentTool = null
                }
            }
            return toolBlocks
        }

        val apiFiles = rootDir.listFiles().orEmpty()
            .filter { it.isDirectory && (it.name == "sentinel" || it.name.startsWith("sentinel-") || it.name.startsWith("tool-")) }
            .map { file("${it.path}/api/${it.name}.api") }
            .filter { it.exists() }
        val (noOpApiFiles, realApiFiles) = apiFiles.partition { it.name.endsWith("-no-op.api") }

        val realTools = realApiFiles.map { extractToolBlocks(it) }.fold(emptyMap<String, Set<String>>()) { acc, it -> acc + it }
        val noOpTools = noOpApiFiles.map { extractToolBlocks(it) }.fold(emptyMap<String, Set<String>>()) { acc, it -> acc + it }

        val sentinelRelevant = extractRelevantLines(sentinelApi)
        val noOpRelevant = extractRelevantLines(noOpApi)

        val missingLines = sentinelRelevant - noOpRelevant
        val missingTools = realTools.keys - noOpTools.keys
        val missingToolLines = realTools
            .filterKeys { it in noOpTools }
            .mapValues { (tool, lines) -> lines - noOpTools.getValue(tool) }
            .filterValues { it.isNotEmpty() }

        if (missingLines.isNotEmpty() || missingTools.isNotEmpty() || missingToolLines.isNotEmpty()) {
            if (missingLines.isNotEmpty()) {
                println("🚨 API mismatch detected! The following APIs in `Sentinel` are missing in `Sentinel no-op`:")
                missingLines.forEach { println("❌ $it") }
            }
            if (missingTools.isNotEmpty()) {
                println("🚨 The following tools have no no-op counterpart:")
                missingTools.forEach { println("❌ $it") }
            }
            missingToolLines.forEach { (tool, lines) ->
                println("🚨 API mismatch detected! The following APIs in `$tool` are missing in its no-op:")
                lines.forEach { println("❌ $it") }
            }
            throw GradleException("API mismatch detected between Sentinel and Sentinel no-op!")
        } else {
            println("✅ API is identical between Sentinel and Sentinel no-op!")
        }
    }
}
