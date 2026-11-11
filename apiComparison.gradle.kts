tasks.register("compareSentinelApis") {
    doFirst {
        val apiFiles = rootDir.listFiles().orEmpty()
            .filter { it.isDirectory && (it.name == "sentinel" || it.name.startsWith("sentinel-") || it.name.startsWith("tool-")) }
            .map { file("${it.path}/api/${it.name}.api") }
            .filter { it.exists() }
        val (noOpApiFiles, realApiFiles) = apiFiles.partition { it.name.endsWith("-no-op.api") }

        if (realApiFiles.isEmpty() || noOpApiFiles.isEmpty()) {
            throw GradleException("API dump files not found. Run `apiDump` task first.")
        }

        // Class blocks keyed by class name, e.g. "com/infinum/sentinel/ui/tools/ChuckerTool"
        fun extractClassBlocks(apiFile: File): Map<String, Set<String>> {
            val classBlocks = mutableMapOf<String, MutableSet<String>>()
            var currentClass: String? = null

            apiFile.forEachLine { line ->
                val trimmed = line.trim()

                if (trimmed.endsWith("{") && trimmed.contains(" class ")) {
                    currentClass = trimmed.substringAfter(" class ").substringBefore(" ")
                }

                currentClass?.let { classBlocks.getOrPut(it) { mutableSetOf() }.add(line) }

                if (trimmed.isEmpty()) {
                    currentClass = null
                }
            }
            return classBlocks
        }

        val realClasses = realApiFiles.map { extractClassBlocks(it) }.fold(emptyMap<String, Set<String>>()) { acc, it -> acc + it }
        val noOpClasses = noOpApiFiles.map { extractClassBlocks(it) }.fold(emptyMap<String, Set<String>>()) { acc, it -> acc + it }

        // Generated classes and internal UI screens are not meant to be referenced by consumers
        val uiScreens = realClasses
            .filterValues { lines -> lines.first().let { it.contains("Activity {") || it.contains("Fragment {") } }
            .keys
        // Registered automatically through the tool's manifest, consumers are not expected to reference it
        val autoRegisteredInitializers = setOf("com/infinum/sentinel/TimberInitializer")
        val checkedClasses = realClasses.filterKeys { name ->
            !name.contains("/databinding/") &&
                !name.endsWith("/BuildConfig") &&
                name.substringBefore("$") !in uiScreens &&
                name !in autoRegisteredInitializers
        }

        val missingClasses = checkedClasses.keys - noOpClasses.keys
        val missingLines = checkedClasses
            .filterKeys { it in noOpClasses }
            .mapValues { (name, lines) -> lines - noOpClasses.getValue(name) }
            .filterValues { it.isNotEmpty() }

        if (missingClasses.isNotEmpty() || missingLines.isNotEmpty()) {
            if (missingClasses.isNotEmpty()) {
                println("🚨 The following classes have no no-op counterpart:")
                missingClasses.forEach { println("❌ $it") }
            }
            missingLines.forEach { (name, lines) ->
                println("🚨 API mismatch detected! The following APIs in `$name` are missing in its no-op:")
                lines.forEach { println("❌ $it") }
            }
            throw GradleException("API mismatch detected between Sentinel and Sentinel no-op!")
        } else {
            println("✅ API is identical between Sentinel and Sentinel no-op!")
        }
    }
}
