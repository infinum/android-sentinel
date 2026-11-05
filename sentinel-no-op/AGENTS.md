# sentinel-no-op

This is the release-variant replacement for `sentinel`. Apps use `debugImplementation(sentinel)` and `releaseImplementation(sentinel-no-op)`, so both must expose **the same FQCNs and signatures**.

- Depends only on kotlin-stdlib and androidx-startup. Do not add Room, DI, UI or any real behaviour.
- `Sentinel.kt` and `SentinelInitializer.kt` mirror the core API with empty bodies. `watch` returns `this`, and `name()` defaults return `0`.
- `ui/tools/` holds stubs for most tool modules: AppGallery, GooglePlay, Chucker, Collar, DbInspector, LeakCanary, Timber and NetworkEmulator, plus `CertificateTool`.
  - The Showkase stub lives in `tool-showkase-no-op` instead.
  - Constructor shapes should match the real tools so that shared registration code compiles in release. Existing gap: the `NetworkEmulatorTool` stub has no parameters, while the real tool takes `name` and `listener`.
- Every stub class must be kept in `proguard-rules.txt`.

## Keeping in sync

1. Change the API in `sentinel` and here together.
2. Run `./gradlew apiDump` and commit `api/sentinel-no-op.api` together with `sentinel/api/sentinel.api`.
3. Run `./gradlew :sentinel:apiCheck`. It runs `compareSentinelApis` (`../apiComparison.gradle.kts`), which fails if any line in the core's `Sentinel`, `SentinelInitializer` or `Sentinel$Tool` blocks is missing here.
   - The comparison is one-way and only covers those blocks. Check tool stubs and the other public types by hand.
