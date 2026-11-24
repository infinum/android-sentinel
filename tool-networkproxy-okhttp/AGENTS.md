# tool-networkproxy-okhttp

This module modifies or mocks API responses through an OkHttp interceptor, with rules edited in a Sentinel screen. User-facing docs: [README.md](README.md).

## Rules

- OkHttp is `compileOnly`, so the app supplies it. Don't change it to `api` or `implementation`.
- The only public API is `NetworkProxyInterceptor(context)` and `NetworkProxyTool`. Keep everything else `internal`, so it needs no no-op counterpart.
  - `NetworkProxyInterceptor` is mirrored in [`tool-networkproxy-okhttp-no-op`](../tool-networkproxy-okhttp-no-op/AGENTS.md), `NetworkProxyTool` in `sentinel-no-op`. Update both and re-run `apiDump`.
- The proxy must never be the reason a call fails. Wrap everything it does in `safely { }` so the real response wins.
- Response bodies are captured by `CapturingSource` while the app reads them. Don't read them up front, since that blocks the app until the whole body has downloaded.
- Rules are stored as JSON in SharedPreferences and outlive app updates. A new `ProxyRule` field needs a default in `RuleJson.fromJson` so older payloads still decode.
- If you change matching, modes, limits or the UI, update `README.md` and, if view ids or visible texts change, the flows in [`.maestro/`](../.maestro/README.md).
- Verify with `./gradlew :tool-networkproxy-okhttp:lintRelease :tool-networkproxy-okhttp:detekt :tool-networkproxy-okhttp:apiCheck`.

## Tests

- `./gradlew :tool-networkproxy-okhttp:testDebugUnitTest` (JUnit4, `MockWebServer`).
- Build the interceptor through its `internal` constructor with a fake `RuleSource` and a new `RecentCallStore`, never `RecentCallStore.shared`.
