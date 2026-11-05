# tool-timber

This is an in-app log viewer built on [Timber](https://github.com/JakeWharton/timber). It lets the user search, filter by tag, browse daily log files and share them. Namespace `com.infinum.sentinel.tool.timber`, with `resourcePrefix = "sentinel_"` and ViewBinding.

## Key files (`src/main/kotlin/com/infinum/sentinel/`)

- `ui/tools/TimberTool.kt`: `TimberTool(allowedTags = emptyList(), listener = default)` with `@JvmOverloads`.
  - `init` stores the tags in the `AllowedTags` singleton (`ui/logger/storage/`).
  - The listener opens `LoggerActivity`.
- `TimberInitializer.kt`: an `androidx.startup` initializer, merged into the manifest's `InitializationProvider`. It plants `SentinelFileTree` automatically, so apps do not plant it themselves.
- `SentinelFileTree.kt`: a `Timber.DebugTree` that pushes entries into an in-memory `FlowBuffer` and appends them to `filesDir/logs/dd-MM-yyyy.log` on `Dispatchers.IO` (see `ui/shared/LogFileResolver.kt`).
- `SentinelLogsProvider.kt`: a `FileProvider` with authority `${applicationId}.sentinel.logprovider` and paths in `res/xml/sentinel_file_paths.xml`, used for sharing log files.
- `ui/logger/` (live log, `LoggerActivity`) and `ui/logs/` (file list, `LogsActivity`) use RecyclerView adapters with DiffUtil.

## Rules

- Both activities use `Sentinel.Theme.Leaf`, taskAffinity `com.infinum.sentinel.logger`, and the `sentinel_infinum_monitored=false` meta-data. Keep these for any new activity.
- Release stub: `sentinel-no-op/.../ui/tools/TimberTool.kt`. It takes `allowedTags`, so keep the constructor in sync.
- Not in root `lintAll`/`detektAll`. Run its checks directly: `./gradlew :tool-timber:lintRelease :tool-timber:detekt :tool-timber:apiCheck`.
- There are no tests.
