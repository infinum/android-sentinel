# Network Emulator Tool

The **Network Emulator Tool** is a built-in Sentinel feature that allows developers to simulate slow and flaky network conditions to test how their applications behave under poor connectivity.

## Features

- **Network Delay**: Add configurable delays (0-10,000ms) to all network requests
- **Fail Percentage**: Configure what percentage of requests should fail (0-100%)
- **Variance Percentage**: Add random variance to delays to simulate unstable connections (0-100%)
- **Easy UI**: Simple toggle and slider-based interface within Sentinel
- **Persistent Settings**: Configuration is saved across app restarts

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

### 3. For Retrofit Users

If you're using Retrofit, add the interceptor when building the OkHttpClient:

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(NetworkEmulatorInterceptor(context))
    .build()

val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .baseUrl("https://api.example.com/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
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

- ⚠️ **Debug builds only**: Ensure the interceptor is only added in debug builds
- 📱 **Device-specific**: Settings are stored per device and persist across app restarts
- 🔄 **Reset available**: Use the "Reset to Defaults" button to restore default settings
- 🎯 **App-specific**: Network emulation only affects your app, not the entire device

## Advanced Usage

### Conditional Setup

Only add the interceptor in debug builds:

```kotlin
val clientBuilder = OkHttpClient.Builder()

if (BuildConfig.DEBUG) {
    clientBuilder.addInterceptor(NetworkEmulatorInterceptor(context))
}

val client = clientBuilder.build()
```

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
- Verify you're using a compatible OkHttp version (4.x)

**Q: Settings don't persist**
- Settings are stored in SharedPreferences and should persist
- Check if your app has proper storage permissions
- Try using "Reset to Defaults" and reconfiguring

## Benefits

- ✅ Test app behavior under poor network conditions
- ✅ Validate loading states and error handling
- ✅ Identify timeout issues
- ✅ Improve user experience for users with slow connections
- ✅ No need for external tools or proxy configuration
- ✅ Works on both emulators and physical devices
