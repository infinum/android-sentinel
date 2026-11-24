# sample

This is the demo and dev app for exercising Sentinel and all tools. It is not published. Namespace `com.infinum.sentinel.sample`. The application ID is `com.infinum.sentinel.sample`, and debug builds add `.debug`.

## Wiring (`build.gradle.kts`)

- `debugImplementation(project(":sentinel"))` and `releaseImplementation(project(":sentinel-no-op"))`.
- Every tool module is `debugImplementation(project(...))`.
  - Release stubs come from `sentinel-no-op`.
  - The exceptions are network emulator and Showkase, which use their own `-no-op` modules in release.
- Commented-out `libs.library` / `libs.librarynoop` lines show the published-artifact alternative.
- Showkase:
  - `kspDebug(libs.showkase.processor)` only. Never use the project-wide `ksp` configuration.
  - `releaseCompileOnly(libs.compose.runtime)`, because the Compose compiler plugin applies to all variants.
- Release is minified and signed with the bundled `dummy.jks`.

## Code

- `SampleApplication.kt`: `getWatchedTools()` registers every tool and calls `Sentinel.watch(...)`. Add new tools here.
- `JavaMainActivity.java` registers tools from Java. Keep it compiling to verify Java interop (`@JvmOverloads`, defaults).
- Feature demos:
  - `BundleActivity` / `BundleTree`: Bundle Monitor
  - `ANRTester`: ANR monitor
  - `ApiClient`: OkHttp, with Chucker and the network emulator interceptors
  - `SampleShowkaseRootModule`, plus the debug-only `src/debug/.../ShowkaseSamples.kt`
- `assets/` holds the certificates used by `CertificateTool` (API 26+).

Verify both variants: `./gradlew :sample:assembleDebug :sample:assembleRelease`. The release build catches API mismatches with the no-op modules.
