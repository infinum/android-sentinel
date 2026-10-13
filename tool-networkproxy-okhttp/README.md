# Network Proxy Tool (OkHttp)

Alters API responses inside the app process, so it works with certificate pinning where a MITM proxy cannot.
Rules are edited in a Sentinel screen and match on HTTP method plus URL path. A rule either modifies the
real response (status code, optionally the body) or mocks it without touching the network.

## Setup

```kotlin
// build.gradle.kts
debugImplementation("com.infinum.sentinel:tool-networkproxy-okhttp:$sentinelVersion")
releaseImplementation("com.infinum.sentinel:tool-networkproxy-okhttp-no-op:$sentinelVersion")
```

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(ChuckerInterceptor(context))
    .addInterceptor(NetworkProxyInterceptor(context))
    .build()

Sentinel.watch(setOf(NetworkProxyTool()))
```

Add the proxy interceptor after Chucker so Chucker records what the app actually received.

## Usage

1. Make the calls you want to alter. They appear under **Seen recently** even while the proxy is off.
2. Tap a recent call to open the editor pre-filled with its method, path, status code and body, or tap **Add rule**.
3. Pick a mode:
   - **Modify real response**: the request goes to the network, then the status code is replaced and, with
     **Replace body** on, the body too.
   - **Mock, skip network**: the request never leaves the app. The rule's status code and body are returned.
4. Turn on the **Enabled** switch in the toolbar.

Path patterns start with `/`. `*` matches exactly one path segment (`/users/*/posts`). Query string and host
are ignored. Rules are checked in list order and the first enabled match wins.

## Limits

- Request modification, header overrides and regex matching are not supported.
- Bodies over 256 KB or with a non-text content type are recorded without a body.
- Rules are stored in SharedPreferences on the device and survive app restarts. Recent calls live in memory.
