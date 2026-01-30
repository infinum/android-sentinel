# Network Emulator Tool (OkHttp)

The **Network Emulator Tool** is a built-in Sentinel feature that allows developers to simulate slow and flaky network conditions to test how their applications behave under poor connectivity. **This tool is specifically designed for OkHttp clients.**

## Features

- **Network Delay**: Add configurable delays (0-10,000ms) to all network requests
- **Fail Percentage**: Configure what percentage of requests should fail (0-100%)
- **Variance Percentage**: Add random variance to delays to simulate unstable connections (0-100%)
- **Easy UI**: Simple toggle and slider-based interface within Sentinel
- **Persistent Settings**: Configuration is saved across app restarts

## ⚠️ Important: Release Build Considerations

The Network Emulator is a development/debugging tool. You have **two options** for handling release builds:

### Option 1: Use the No-op Module

The **easiest and cleanest approach** is to use the no-op version for release builds. This gives you:
- ✅ **Same code everywhere** - no separate source sets needed
- ✅ **API compatibility** - identical classes and methods
- ✅ **Zero overhead** - no functionality in release builds

**1. Add both dependencies in your `build.gradle`:**
```gradle
dependencies {
    debugImplementation "com.infinum.sentinel:tool-networkemulator-okhttp:x.x.x"
    releaseImplementation "com.infinum.sentinel:tool-networkemulator-okhttp-no-op:x.x.x"
}
```

**2. Use the same code everywhere:**
```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(NetworkEmulatorInterceptor(context))
    .build()
```

That's it! Debug builds get the full functionality, release builds get a pass-through interceptor.

👉 **[See full no-op documentation](../tool-networkemulator-okhttp-no-op/README.md)**

### Option 2: Separate Source Sets (Debug-Only)

Alternatively, use Android build variants to **completely exclude** the tool from release builds:

**1. Add the dependency only for debug builds** in your `build.gradle`:
```gradle
dependencies {
    debugImplementation "com.infinum.sentinel:tool-networkemulator-okhttp:x.x.x"
}
```

**2. Create separate source sets** for debug and release:

**`src/debug/kotlin/YourApiClient.kt`** (with interceptor):
```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(NetworkEmulatorInterceptor(context))
    .build()
```

**`src/release/kotlin/YourApiClient.kt`** (without interceptor):
```kotlin
val client = OkHttpClient.Builder()
    // No network emulator in release builds
    .build()
```

This ensures the code is completely excluded from release builds at compile time.


## Setup

### 1. Add the Tool to Sentinel

When initializing Sentinel, add the `NetworkEmulatorTool`:

**Kotlin:**
```kotlin
Sentinel.watch(
    tools = setOf(
        NetworkEmulatorTool()
    )
)
```

**Java:**
```java
Set<Sentinel.Tool> tools = new HashSet<>();
tools.add(new NetworkEmulatorTool());
Sentinel.watch(tools, new HashMap<>());
```

### 2. Add the Interceptor to OkHttpClient

Add the `NetworkEmulatorInterceptor` to your OkHttp client:

**Kotlin:**
```kotlin
val client = OkHttpClient.Builder()
    .addInterceptor(NetworkEmulatorInterceptor(context))
    .build()
```

**Java:**
```java
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new NetworkEmulatorInterceptor(context))
    .build();
```

## Usage

1. **Open Sentinel**: Trigger Sentinel using your configured trigger (shake, manual, etc.)
2. **Navigate to Tools**: Tap on the "Tools" tab
3. **Open Network Emulator**: Tap on "Network Emulator" button
4. **Configure Settings**:
   - Enable network emulation with the toggle switch
   - Adjust delay slider (in milliseconds)
   - Set fail percentage (0-100%)
   - Set variance percentage for unpredictable delays
5. **Test Your App**: Navigate through your app and observe how it behaves

## Configuration Details

### Network Delay
- **Range**: 0 - 10,000 milliseconds
- **Step**: 100ms
- **Default**: 1,000ms
- **Purpose**: Adds a fixed delay to every network request to simulate slow networks

### Fail Percentage
- **Range**: 0 - 100%
- **Step**: 5%
- **Default**: 0%
- **Purpose**: Determines what percentage of requests should fail with a 500 error

### Variance Percentage
- **Range**: 0 - 100%
- **Step**: 5%
- **Default**: 0%
- **Purpose**: Adds random variance to delays (±percentage of base delay) to simulate unstable connections

### Example Scenarios

**Slow but stable 3G network:**
- Delay: 3000ms
- Fail Percentage: 0%
- Variance: 10%

**Flaky connection:**
- Delay: 2000ms
- Fail Percentage: 30%
- Variance: 50%

**Extremely poor network:**
- Delay: 5000ms
- Fail Percentage: 50%
- Variance: 80%

## Important Notes

- 📱 **Device-specific**: Settings are stored per device and persist across app restarts
- 🔄 **Reset available**: Use the "Reset to Defaults" button to restore default settings

## Advanced Usage

### Alternative: Runtime Conditional Setup

If you can't use build variants or the no-op module, you can conditionally add the interceptor at runtime:

```kotlin
val clientBuilder = OkHttpClient.Builder()

// Note: This approach still includes the code in release builds
if (BuildConfig.DEBUG) {
    clientBuilder.addInterceptor(NetworkEmulatorInterceptor(context))
}

val client = clientBuilder.build()
```

⚠️ **Warning**: This approach still bundles the Network Emulator code in release builds. **Prefer using the no-op module** (Option 1 above) or build variants with `debugImplementation` for cleaner separation.

### Programmatic Access

You can also access the preferences programmatically:

```kotlin
val preferences = NetworkEmulatorPreferences(context)

// Check if enabled
if (preferences.isEnabled) {
    // Network emulation is active
}

// Modify settings programmatically
preferences.delayMs = 2000
preferences.failPercentage = 20
preferences.variancePercentage = 30
```

## Troubleshooting

**Q: Network emulation doesn't seem to work**
- Verify the interceptor is added to your OkHttpClient
- Ensure the "Enable Network Emulation" toggle is on in the UI
- Check that you're making requests with the OkHttpClient that has the interceptor

**Q: App crashes with OkHttp errors**
- Ensure OkHttp dependency is included in your project
- Verify you're using a compatible OkHttp version

