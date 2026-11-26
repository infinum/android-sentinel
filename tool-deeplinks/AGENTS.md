# tool-deeplinks

This module lists the deep links the app declares in its manifest. Testers save variants with their own query parameters and launch them. Namespace `com.infinum.sentinel.tool.deeplinks`, with `resourcePrefix = "sentinel_"`. It is the only module with a Compose UI: Compose BOM, Material 3 and Navigation 3. Core `sentinel` stays free of Compose. User-facing docs are in the Deep links section of the root `README.md`.

## Key files (`src/main/kotlin/com/infinum/sentinel/`)

- `ui/tools/DeepLinkTool.kt`: `DeepLinkTool(exampleLinks, listener)`. Its `init` fills `ExampleLinksRegistry`, and the listener opens `DeepLinksActivity`.
- `data/sources/raw/deeplinks/ManifestReader.kt` parses the compiled `AndroidManifest.xml` of the base APK. It finds the APK's asset cookie through `R.drawable.sentinel_ic_launch`, so keep that drawable unqualified in `res/drawable/`.
- `domain/deeplinks/discovery/` turns intent filters into base links, warnings and templates. `domain/deeplinks/uri/` parses, encodes and builds links.
- Variants are stored as versioned JSON in the SharedPreferences file `sentinel_deep_links` (`DeepLinkVariantsSerializer`).
- `ui/deeplinks/`: one `DeepLinksActivity`, with Navigation 3 between the list, the breakdown and the editor. Each screen has its own view model built on `DeepLinksBaseViewModel`.
- `ui/deeplinks/di/DeepLinksGraph.kt` wires dependencies by hand. The activity calls `init` before `setContent`. There is no kotlin-inject or KSP here.

## Rules

- No class may have the fully qualified name of a core class, because both end up on the app's classpath. `com.infinum.sentinel.ui.tools` holds only `DeepLinkTool`, since minified core shares that package.
- Resources: the module's own `R` is `com.infinum.sentinel.tool.deeplinks.R`. Core resources come in through `import com.infinum.sentinel.R as SentinelR`.
- The activity uses `Sentinel.Theme.Leaf`, taskAffinity `com.infinum.sentinel.deeplinks`, and the `sentinel_infinum_monitored=false` meta-data.
- Release stub: `DeepLinkTool` in `sentinel-no-op`, a `data class` with the same constructor. Keep the two in sync and re-dump both `.api` files.
- API dumps leave out `com.infinum.sentinel.ui.deeplinks` (root `apiValidation`), because the Compose compiler makes public `ComposableSingletons` classes there. `compareSentinelApis` skips the `$stable` fields Compose adds.
- Tests:
  - JVM tests are in `src/test` and use `runBlocking`. kotlinx-coroutines-test isn't a dependency.
  - Instrumented tests are in `src/androidTest`, and their fixture activities are declared in `src/androidTest/AndroidManifest.xml`. They run from the test APK `com.infinum.sentinel.tool.deeplinks.test`.
- Verify with `./gradlew :tool-deeplinks:testDebugUnitTest :tool-deeplinks:lintRelease :tool-deeplinks:detekt apiCheck`.
