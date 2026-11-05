# tool-collar

This module wraps [Collar](https://github.com/infinum/android-collar), Infinum's analytics event viewer. Namespace `com.infinum.sentinel.tool.collar`.

- `src/main/kotlin/com/infinum/sentinel/ui/tools/CollarTool.kt`
  - `CollarTool(listener = default)`
  - Opens `CollarUi.launchIntent()`.
- The title comes from overriding the core `sentinel_analytics` string (`tools:override="true"`).
- Dependencies: `api(libs.library)` and `api(libs.collar)` (`collar-ui`).
- The release stub lives in `sentinel-no-op/.../ui/tools/CollarTool.kt`, so keep the constructor in sync.
- There are no tests. Verify with `./gradlew :tool-collar:lintRelease :tool-collar:detekt :tool-collar:apiCheck`.
