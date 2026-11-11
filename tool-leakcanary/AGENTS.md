# tool-leakcanary

This module wraps [LeakCanary](https://square.github.io/leakcanary/). Namespace `com.infinum.sentinel.tool.leakcanary`.

- `src/main/kotlin/com/infinum/sentinel/ui/tools/LeakCanaryTool.kt`, `LeakCanaryTool(listener = default)`.
  - **Its `init` has side effects:** it hides LeakCanary's launcher icon and sets `dumpHeap = false`, so heap dumps happen only on demand.
  - On click, the listener enables heap dumping, calls `LeakCanary.dumpHeap()` and opens the leak display activity.
- The title comes from overriding the core `sentinel_memory` string (`tools:override="true"`).
- Dependencies: `api(libs.library)` and `api(libs.leakcanary)`.
- The release stub lives in `sentinel-no-op/.../ui/tools/LeakCanaryTool.kt`, so keep the constructor in sync.
- There are no tests. Verify with `./gradlew :tool-leakcanary:lintRelease :tool-leakcanary:detekt :tool-leakcanary:apiCheck`.
