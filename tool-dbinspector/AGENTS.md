# tool-dbinspector

This module wraps [DbInspector](https://github.com/infinum/android_dbinspector), the on-device SQLite browser. Namespace `com.infinum.sentinel.tool.dbinspector`.

- `src/main/kotlin/com/infinum/sentinel/ui/tools/DbInspectorTool.kt`
  - `DbInspectorTool(listener = default)`
  - Calls `DbInspector.show()`.
- The title comes from overriding the core `sentinel_database` string (`tools:override="true"`).
- Dependencies: `api(libs.library)` and `api(libs.dbinspector)`.
- The release stub lives in `sentinel-no-op/.../ui/tools/DbInspectorTool.kt`, so keep the constructor in sync.
- This is the simplest tool module and the recommended template when adding a new one (see the root `AGENTS.md`).
- There are no tests. Verify with `./gradlew :tool-dbinspector:lintRelease :tool-dbinspector:detekt :tool-dbinspector:apiCheck`.
