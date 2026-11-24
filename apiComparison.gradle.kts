tasks.register("compareSentinelApis") {
    val apiFiles = rootProject.subprojects
        .filter { it.name == "sentinel" || it.name.startsWith("sentinel-") || it.name.startsWith("tool-") }
        .map { File(it.projectDir, "api/${it.name}.api") }

    doFirst {
        val missingApiFiles = apiFiles.filterNot { it.exists() }
        if (missingApiFiles.isNotEmpty()) {
            throw GradleException(
                "API dump files not found: ${missingApiFiles.joinToString { it.name }}. Run `apiDump` task first."
            )
        }
        val (noOpApiFiles, realApiFiles) = apiFiles.partition { it.name.endsWith("-no-op.api") }

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

        // A class defined in more than one module would clash on the app's classpath, and merging would hide one of its blocks
        fun mergeClassBlocks(apiFiles: List<File>): Map<String, Set<String>> {
            val blocksPerFile = apiFiles.associate { it.name to extractClassBlocks(it) }
            val duplicateClasses = blocksPerFile
                .flatMap { (fileName, blocks) -> blocks.keys.map { it to fileName } }
                .groupBy({ it.first }, { it.second })
                .filterValues { it.size > 1 }
            if (duplicateClasses.isNotEmpty()) {
                println("🚨 The following classes are defined in more than one module:")
                duplicateClasses.forEach { (name, fileNames) -> println("❌ $name in ${fileNames.joinToString()}") }
                throw GradleException("Duplicate classes detected across modules!")
            }
            return blocksPerFile.values.fold(emptyMap()) { acc, it -> acc + it }
        }

        val realClasses = mergeClassBlocks(realApiFiles)
        val noOpClasses = mergeClassBlocks(noOpApiFiles)

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
            throw GradleException("API mismatch detected between real and no-op modules!")
        } else {
            println("✅ API is identical between real and no-op modules!")
        }
    }
}
