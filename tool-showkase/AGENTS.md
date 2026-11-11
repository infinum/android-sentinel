# tool-showkase

This module is a launcher for the [Airbnb Showkase](https://github.com/airbnb/Showkase) browser. It only starts `ShowkaseBrowserActivity`; the app owns Compose, the KSP processor and the Showkase runtime. Namespace `com.infinum.sentinel.tool.showkase`. User-facing setup: [README.md](README.md).

- `src/main/kotlin/com/infinum/sentinel/ui/tools/ShowkaseTool.kt`: `ShowkaseTool(rootModule: KClass<out ShowkaseRootModule>)`.
  - It catches `NoClassDefFoundError` when the app didn't add the runtime, and logs to the `Sentinel` logcat tag instead of crashing.
- Dependencies:
  - `api(libs.showkase.annotation)`: a Kotlin-only artifact, so `@ShowkaseRoot` resolves in every variant.
  - `compileOnly(libs.showkase)`: the runtime, which pulls in Compose.
  - Keep both this way. This module applies neither the Compose nor the KSP plugin.
- `consumer-rules.pro` ships keep rules for `ShowkaseRootModule` implementations and `@ShowkaseRootCodegen` classes, because Showkase loads them reflectively. `proguard-rules.txt` covers only this module's own release build.
- The release stub is [`tool-showkase-no-op`](../tool-showkase-no-op/AGENTS.md), not `sentinel-no-op`.
- See `sample` for the reference wiring: `kspDebug` for the processor, never the project-wide `ksp`.
- There are no tests. Verify with `./gradlew :tool-showkase:lintRelease :tool-showkase:detekt :tool-showkase:apiCheck`.
