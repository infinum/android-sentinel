# tool-chucker

This module wraps [Chucker](https://github.com/ChuckerTeam/chucker), the HTTP inspector. Namespace `com.infinum.sentinel.tool.chucker`.

- `src/main/kotlin/com/infinum/sentinel/ui/tools/ChuckerTool.kt`
  - `ChuckerTool(listener = default)`
  - Opens `Chucker.getLaunchIntent(context)`.
- The title comes from overriding the core `sentinel_network` string (`tools:override="true"` in `res/values/strings.xml`).
- Dependencies: `api(libs.library)` and `api(libs.chucker)`. The app adds `ChuckerInterceptor` to its own OkHttp client.
- The release stub lives in `sentinel-no-op/.../ui/tools/ChuckerTool.kt`, so keep the constructor in sync.
- There are no tests. Verify with `./gradlew :tool-chucker:lintRelease :tool-chucker:detekt :tool-chucker:apiCheck`.
