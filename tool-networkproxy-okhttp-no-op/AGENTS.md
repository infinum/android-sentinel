# tool-networkproxy-okhttp-no-op

This module is the release stub for [`tool-networkproxy-okhttp`](../tool-networkproxy-okhttp/AGENTS.md). `NetworkProxyInterceptor(context)` just calls `chain.proceed(request)`. User-facing docs: [README.md](README.md).

- It does **not** depend on `sentinel`. `NetworkProxyTool`'s stub is in `sentinel-no-op`, not here.
- Mirror any public signature change in the real module here, then run `./gradlew apiDump`.
- Verify with `./gradlew :tool-networkproxy-okhttp-no-op:lintRelease :tool-networkproxy-okhttp-no-op:detekt :tool-networkproxy-okhttp-no-op:apiCheck`.
