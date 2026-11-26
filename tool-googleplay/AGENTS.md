# tool-googleplay

This module opens the host app's Google Play listing. Namespace `com.infinum.sentinel.tool.googleplay`.

- `src/main/kotlin/com/infinum/sentinel/ui/tools/GooglePlayTool.kt`, `GooglePlayTool(listener = default)`. It uses `context.packageName`.
  - It resolves `market://details?id=<pkg>` and launches `com.android.vending` directly if that is installed.
  - Otherwise it falls back to the `https://play.google.com/store/apps/details?id=<pkg>` URL.
  - It uses the `ResolveInfoFlags` overload on API 33+ and the deprecated one below that.
- The title is the module's own `sentinel_google_play` string.
- Dependencies: `api(libs.library)` only.
- This module mirrors `tool-appgallery`, so apply logic fixes to both.
- The release stub lives in `sentinel-no-op/.../ui/tools/GooglePlayTool.kt`.
- There are no tests. Verify with `./gradlew :tool-googleplay:lintRelease :tool-googleplay:detekt :tool-googleplay:apiCheck`.
