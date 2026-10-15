# Network Proxy Tool (OkHttp) - No-op

Pass-through `NetworkProxyInterceptor` with the same constructor as the real module, for release builds.

```kotlin
debugImplementation("com.infinum.sentinel:tool-networkproxy-okhttp:$sentinelVersion")
releaseImplementation("com.infinum.sentinel:tool-networkproxy-okhttp-no-op:$sentinelVersion")
```

The no-op `NetworkProxyTool` comes from `sentinel-no-op`, not from this module, so
`Sentinel.watch(setOf(NetworkProxyTool()))` compiles in both build types. The stub only has the no-argument
constructor, so pass a custom name or listener only from a debug source set.
