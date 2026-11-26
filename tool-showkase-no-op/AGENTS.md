# tool-showkase-no-op

This module is the release stub for [`tool-showkase`](../tool-showkase/AGENTS.md). Its `ShowkaseTool` has the same FQCN and constructor (`rootModule: KClass<out ShowkaseRootModule>`), returns `name() = 0` and has an empty listener.

- `compileOnly(libs.library)`: release apps get `Sentinel` from `sentinel-no-op`, so do **not** change this to `api`, or it would drag the real library into release.
- `api(libs.showkase.annotation)` keeps the app's `@ShowkaseRoot` class compiling in release.
- Keep the constructor and data-class shape identical to the real module. Then run `./gradlew apiDump` and check `api/tool-showkase-no-op.api` against `tool-showkase/api/tool-showkase.api`.
- There is no manifest and no resources. `ShowkaseTool` is kept in `proguard-rules.txt`.
