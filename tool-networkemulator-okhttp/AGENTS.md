# tool-networkemulator-okhttp

This module simulates slow or flaky networking through an OkHttp interceptor, configured from a Sentinel screen. Namespace `com.infinum.sentinel.tool.networkemulator`, with `resourcePrefix = "sentinel_"` and ViewBinding. User-facing docs: [README.md](README.md).

## Key files (`src/main/kotlin/com/infinum/sentinel/`)

- `ui/tools/NetworkEmulatorTool.kt`: `NetworkEmulatorTool(name, listener)` implements `Sentinel.NetworkTool` and opens `NetworkEmulatorActivity`.
- `domain/networkemulator/NetworkEmulatorInterceptor.kt`: `NetworkEmulatorInterceptor(context)`.
  - When the emulator is enabled, it sleeps for `delay ± variance%`.
  - Then, with probability `failPercentage`, it returns a synthetic HTTP 500.
- `domain/networkemulator/NetworkEmulatorPreferences.kt` is the SharedPreferences file `sentinel_network_emulator`. It holds:
  - `isEnabled`
  - `delayMs` (0–15000)
  - `failPercentage` and `variancePercentage` (0–100)
  - `reset()`
- `ui/networkemulator/`: the Activity and Fragment, with layout `sentinel_fragment_network_emulator.xml`.

## Rules

- OkHttp is `compileOnly`, so the app supplies it. Don't change it to `api` or `implementation`.
- Public API parity:
  - `NetworkEmulatorInterceptor` and `NetworkEmulatorPreferences` are mirrored in [`tool-networkemulator-okhttp-no-op`](../tool-networkemulator-okhttp-no-op/AGENTS.md).
  - `NetworkEmulatorTool` is mirrored in `sentinel-no-op`. That stub currently has no constructor parameters.
  - Update the stubs and re-run `apiDump` for both modules.
- The activity uses `Sentinel.Theme.Leaf` and the `sentinel_infinum_monitored=false` meta-data.
- If you change slider ranges or behaviour, update `README.md` too.
- Not in root `lintAll`/`detektAll`. Run its checks directly: `./gradlew :tool-networkemulator-okhttp:lintRelease :tool-networkemulator-okhttp:detekt :tool-networkemulator-okhttp:apiCheck`.
- There are no tests.
