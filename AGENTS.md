# AGENTS.md

Sentinel is an Infinum Android **library** (published to Maven Central as `com.infinum.sentinel:*`): a debug-only overlay that shows app, device, permission and preference info and hosts pluggable "tools". Apps ship `sentinel` in debug and `sentinel-no-op` in release.

## Modules

Each module has its own `AGENTS.md` with the details. **Read it before changing that module.**

| Module | Role |
| --- | --- |
| [`sentinel`](sentinel/AGENTS.md) | Core library: public API, UI, triggers, collectors, formatters, crash/ANR/bundle monitors |
| [`sentinel-no-op`](sentinel-no-op/AGENTS.md) | Release stub mirroring the public API, plus no-op stubs of most tool classes |
| [`sample`](sample/AGENTS.md) | Demo app wiring every module (debug = real, release = no-op) |
| [`tool-chucker`](tool-chucker/AGENTS.md), [`tool-collar`](tool-collar/AGENTS.md), [`tool-dbinspector`](tool-dbinspector/AGENTS.md), [`tool-leakcanary`](tool-leakcanary/AGENTS.md) | Thin launchers for third-party debug libraries |
| [`tool-googleplay`](tool-googleplay/AGENTS.md), [`tool-appgallery`](tool-appgallery/AGENTS.md) | Open the app's store listing |
| [`tool-timber`](tool-timber/AGENTS.md) | In-app log viewer backed by a Timber tree |
| [`tool-networkemulator-okhttp`](tool-networkemulator-okhttp/AGENTS.md) (+ [`-no-op`](tool-networkemulator-okhttp-no-op/AGENTS.md)) | OkHttp interceptor that adds delay/failures, with a settings UI |
| [`tool-showkase`](tool-showkase/AGENTS.md) (+ [`-no-op`](tool-showkase-no-op/AGENTS.md)) | Launcher for the Airbnb Showkase browser |

`tool-networkproxy-okhttp*` directories contain only stale build output and are not part of the build; ignore them.

## Build and verify

- Requires JDK 17+ to run Gradle; modules compile with a JVM 11 toolchain. minSdk 24, compile/targetSdk 37.
- Static checks (what CI runs, `.github/workflows/code-analysis.yml`): `./gradlew lintAll detektAll` (or `runStaticChecks`).
  - `detekt` has `autoCorrect = true` and **rewrites files**. Config: `config/detekt.yml` (maxIssues 0).
- Build the sample: `./gradlew :sample:assembleDebug :sample:assembleRelease`.
- Tests exist only in `sentinel`. See [sentinel/AGENTS.md](sentinel/AGENTS.md). CI does not run tests or `apiCheck`, so run them locally.
- Public API dumps: `./gradlew apiDump` / `apiCheck` (binary-compatibility-validator). Every library module commits `api/<module>.api`, and `publish*` tasks depend on `apiCheck`.
- Publish locally: `./gradlew deployDebug` (Maven Local). Release publishing (`deployAll`) is done by maintainers only.

## Conventions

- Kotlin only in libraries, compiled with `-Xexplicit-api=strict`: every declaration needs an explicit visibility modifier and public return types.
- Resource names are prefixed `sentinel_` (`resourcePrefix` is enforced where set). Shared tool titles reuse core strings via `tools:override="true"`.
- Style: `.editorconfig` (4 spaces, max line length 140) plus detekt-formatting. There is no separate ktlint task.
- Dependencies come from the version catalog `gradle/libs.versions.toml`. Shared SDK and version values come from `config.gradle.kts`, via `extra["buildConfig"]` and `extra["releaseConfig"]`.
- Tool modules depend on the core through the catalog coordinate `api(libs.library)`, not `project(":sentinel")`. If a tool needs unreleased core API, publish locally first with `./gradlew deployDebug`.
- Tool classes live in package `com.infinum.sentinel.ui.tools`, regardless of module.
- Module build scripts are hand-written copies with no convention plugins. `buildSrc` holds only Sonatype config. Copy the nearest sibling module when adding one.
- Never read, print or commit `publish.properties`: it is gitignored and holds credentials.

## Cross-module rules

- **Public API change in `sentinel`**: mirror it in `sentinel-no-op`, then run `./gradlew apiDump` and commit both `.api` files. `:sentinel:apiCheck` runs `compareSentinelApis` (`apiComparison.gradle.kts`), which fails if the no-op misses anything from `Sentinel`, `SentinelInitializer` or `Sentinel$Tool`.
- **Tool public API change**: update the matching no-op stub (in `sentinel-no-op` or the tool's own `-no-op` module) so app code compiles in release, and re-dump `.api`.
- **Version bump**: keep three places in sync:
  - `major`/`minor`/`patch` in `config.gradle.kts`
  - `sentinel` in `libs.versions.toml`
  - a new `## Version X.Y.Z` / `_YYYY-MM-DD_` entry at the top of `CHANGELOG.md`

  `README.md`'s `sentinelVersion = "..."` lines are rewritten automatically by `generateReadme` on build; don't change their format.
- **Adding a tool module `tool-foo`**: copy `tool-dbinspector`, then register it in each of these places:
  - `settings.gradle.kts`
  - `libs.versions.toml`: the library alias and the `tools` bundle
  - root `build.gradle.kts`: the `lintAll` and `detektAll` lists, which are hard-coded
  - `deploy.gradle.kts`: a `deployToolFoo` task, plus `deployToolsAll`, `deployAll` and `deployDebug`
  - a no-op stub, either in `sentinel-no-op` or as a new `tool-foo-no-op` module
  - the sample: `sample/build.gradle.kts`, `SampleApplication.kt` and `JavaMainActivity.java`
  - the module and tool lists and the dependency snippets in `README.md`, and `CHANGELOG.md`
  - a `tool-foo/AGENTS.md`

  Then run `./gradlew :tool-foo:apiDump`.

## Pull requests

Target `master`. Follow `.github/PULL_REQUEST_TEMPLATE` (summary, change type, breaking-change checkbox).
