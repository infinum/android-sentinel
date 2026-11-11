# sentinel-no-op

This is the release-variant replacement for `sentinel`. Apps use `debugImplementation(sentinel)` and `releaseImplementation(sentinel-no-op)`, so both must expose **the same FQCNs and signatures**.

- Depends only on kotlin-stdlib and androidx-startup. Do not add Room, DI, UI or any real behaviour.
- `Sentinel.kt` and `SentinelInitializer.kt` mirror the core API with empty bodies. `watch` returns `this`, and `name()` defaults return `0`.
- `ui/tools/` holds stubs for most tool modules: AppGallery, GooglePlay, Chucker, Collar, DbInspector, LeakCanary, Timber and NetworkEmulator, plus `CertificateTool`.
  - The Showkase stub lives in `tool-showkase-no-op` instead.
  - Each stub matches its real tool exactly: `data class` when the real one is, the same constructor parameters and defaults, the same `public val` properties, the same supertype (`NetworkEmulatorTool` implements `Sentinel.NetworkTool`) and an `icon()` override returning `null`. Unused `private val` parameters need `@Suppress("UnusedPrivateMember")`.
- Public resources in `res/values/public.xml` mirror the core's (`sentinel_name`, `sentinel_infinum_monitored`), with the strings defined in `res/values/strings.xml`.
- Every stub class must be kept in `proguard-rules.txt`.

## Keeping in sync

1. Change the API in `sentinel` (or a tool module) and here together.
2. Run `./gradlew apiDump` and commit `api/sentinel-no-op.api` together with the changed real module's `.api`.
3. Run `./gradlew :sentinel:apiCheck`. It runs `compareSentinelApis` (`../apiComparison.gradle.kts`), which fails if a public class in `sentinel` or any `tool-*` module has no counterpart in a `-no-op` module, or if any of its `.api` lines are missing there.
   - The comparison is line-by-line, so generated members count too: a real `data class` needs a `data class` stub to produce the same `copy`, `componentN`, `equals`, `hashCode` and `toString`.
   - Excluded: `databinding` classes, `BuildConfig`, `Activity`/`Fragment` screens (and their nested classes), and `TimberInitializer`.
   - The comparison is one-way: extra API in the no-op is not reported. Public resources are not checked, so keep `public.xml` in sync by hand.
