# Network Emulator Tool (OkHttp) - No-op

This is a **no-op (no operation)** implementation of the Network Emulator Tool for Sentinel. It provides the same API as the full implementation but with zero functionality—perfect for release builds.

## Setup

### Build Variants 

The cleanest approach is to use build variants to swap implementations:

**1. Add dependencies based on build type in `build.gradle`:**

```gradle
dependencies {
    debugImplementation "com.infinum.sentinel:tool-networkemulator-okhttp:x.x.x"
    releaseImplementation "com.infinum.sentinel:tool-networkemulator-okhttp-no-op:x.x.x"
}
```

**2. Use the same code in your app (works for both debug and release):**

```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(NetworkEmulatorInterceptor(context))
    .build()
```

With this setup:
- **Debug builds** get the full network emulator with UI and functionality
- **Release builds** get the no-op version that does nothing
- **Your code** stays the same—no build variant source sets needed!

## What's Included

The no-op module provides stub implementations of:

- **`NetworkEmulatorInterceptor`**: Passes requests through without modification
- **`NetworkEmulatorPreferences`**: All properties return default/disabled values; setters do nothing

**Note**: The `NetworkEmulatorTool` class is provided by the `sentinel-no-op` module, not this module. When using `releaseImplementation "com.infinum.sentinel:sentinel-no-op:x.x.x"`, you automatically get a no-op version of all tools, including NetworkEmulatorTool.

## Migration from Debug-Only to No-op

If you currently use `debugImplementation` with separate source sets, migrating to the no-op approach is simple:

**Before:**
```gradle
dependencies {
    debugImplementation "com.infinum.sentinel:tool-networkemulator-okhttp:x.x.x"
    // No release dependency
}
```

**After:**
```gradle
dependencies {
    debugImplementation "com.infinum.sentinel:tool-networkemulator-okhttp:x.x.x"
    releaseImplementation "com.infinum.sentinel:tool-networkemulator-okhttp-no-op:x.x.x"
}
```

Then you can delete your `src/release/` source set if you had one—the no-op dependency handles it for you.

## Important Notes

- ⚠️ The no-op version does **not** include any UI components or resources
- 📦 The no-op version has minimal dependencies (only Kotlin stdlib and Sentinel library)
- 🔒 Safe for obfuscation/ProGuard—includes necessary keep rules
- 🎯 Designed for production use with zero debugging footprint

## When to Use Each Implementation

| Scenario | Use This |
|----------|----------|
| Local development | `tool-networkemulator-okhttp`  |
| QA/Testing builds | `tool-networkemulator-okhttp`  |
| Production/Release builds | `tool-networkemulator-okhttp-no-op` (this module) |

## Example Full Setup

Here's a complete example of using both versions:

**`app/build.gradle`:**
```gradle
dependencies {
    // Sentinel core
    debugImplementation "com.infinum.sentinel:sentinel:x.x.x"
    releaseImplementation "com.infinum.sentinel:sentinel-no-op:x.x.x"
    
    // Network Emulator Tool
    debugImplementation "com.infinum.sentinel:tool-networkemulator-okhttp:x.x.x"
    releaseImplementation "com.infinum.sentinel:tool-networkemulator-okhttp-no-op:x.x.x"
}
```

**`YourApiClient.kt`:**
```kotlin
class ApiClient(context: Context) {
    val client = OkHttpClient.Builder()
        .addInterceptor(NetworkEmulatorInterceptor(context))
        .build()
}
```

**`YourApplication.kt`:**
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        Sentinel.watch(
            tools = setOf(
                NetworkEmulatorTool()
            )
        )
    }
}
```

This setup works identically in both debug and release, with the only difference being what actually runs behind the scenes.

## Troubleshooting

**Q: I'm getting compilation errors after adding the no-op dependency**
- Ensure you're using the same version for both the debug and no-op implementations
- Verify the package names match (`com.infinum.sentinel.domain.networkemulator` and `com.infinum.sentinel.ui.tools`)

**Q: The network emulator still appears in release builds**
- If you're registering `NetworkEmulatorTool()` in your application, the no-op version will appear but do nothing
- To completely hide it in release builds, wrap the registration in a debug check or use build variants for your Application class

## Further Reading

For full documentation on the Network Emulator Tool (debug version), see the main module README:
[tool-networkemulator-okhttp/README.md](../tool-networkemulator-okhttp/README.md)
