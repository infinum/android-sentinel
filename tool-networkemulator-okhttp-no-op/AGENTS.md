# tool-networkemulator-okhttp-no-op

This module is the release stub for [`tool-networkemulator-okhttp`](../tool-networkemulator-okhttp/AGENTS.md). It uses the same namespace and FQCNs, and the two modules are never on the same classpath. User-facing docs: [README.md](README.md).

- `NetworkEmulatorInterceptor(context)` just calls `chain.proceed(request)`.
- In `NetworkEmulatorPreferences(context)`, getters return `false`/`0` and setters and `reset()` do nothing.
- There is no manifest and no resources. Dependencies are `kotlin.core` and `compileOnly(libs.okhttp)`. It does **not** depend on `sentinel`.
- `NetworkEmulatorTool`'s stub is in `sentinel-no-op`, not here.
- Any public signature change in the real module must be mirrored here. Then run `./gradlew apiDump` and keep `api/tool-networkemulator-okhttp-no-op.api` consistent with the real module's dump. Both classes are kept in `proguard-rules.txt`.
- Not in root `lintAll`/`detektAll`. Run its checks directly: `./gradlew :tool-networkemulator-okhttp-no-op:lintRelease :tool-networkemulator-okhttp-no-op:detekt :tool-networkemulator-okhttp-no-op:apiCheck`.
- There are no tests.
