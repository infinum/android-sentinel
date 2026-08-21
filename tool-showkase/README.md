# Showkase Tool

A wrapper that opens [Showkase](https://github.com/airbnb/Showkase), Airbnb's Compose component
browser, from Sentinel's Tools screen. Showkase renders your `@Preview` composables, colors and
typography as a browsable, on-device design-system catalogue.

The tool is only a launcher. Showkase's annotation processor has to run over your own sources, so
the Showkase build setup stays in your app.

## Setup

**`app/build.gradle`**

```groovy
def sentinelVersion = "2.0.0"
def showkaseVersion = "1.0.5"

dependencies {
    debugImplementation   "com.infinum.sentinel:tool-showkase:$sentinelVersion"
    releaseImplementation "com.infinum.sentinel:tool-showkase-no-op:$sentinelVersion"

    // The browser runtime. It carries the whole Compose stack, so keep it out of release.
    debugImplementation   "com.airbnb.android:showkase:$showkaseVersion"

    // The processor. Use a variant-scoped configuration, never the project-wide `ksp`.
    kspDebug              "com.airbnb.android:showkase-processor:$showkaseVersion"
}
```

Declare your root module once, anywhere in `main` sources:

```kotlin
import com.airbnb.android.showkase.annotation.ShowkaseRoot
import com.airbnb.android.showkase.annotation.ShowkaseRootModule

@ShowkaseRoot
class MyRootModule : ShowkaseRootModule
```

Register the tool:

```kotlin
Sentinel.watch(
    setOf(
        ShowkaseTool(MyRootModule::class),
    ),
)
```

Your root module, previews and registration all stay in `main` and are shared by every variant.
The Showkase annotations resolve everywhere, release included, because both tool modules expose
`showkase-annotation` transitively, a Kotlin-only artifact with no Compose in it.

For flavored projects, scope the same dependencies per flavor instead of per build type, for
example `developmentImplementation` and `kspDevelopment`. KSP creates one `ksp<Variant>`
configuration per source set, so flavor, build type and combined variants are all available.

### ⚠️ Do not use the project-wide `ksp` configuration

```groovy
// Wrong: applies the processor to every variant, production included.
ksp "com.airbnb.android:showkase-processor:1.0.5"
```

Code generated for production references the browser runtime, so either the Compose payload ships
to production too, or production stops compiling. Always name the variant, such as `kspDebug`.

## Compose stays yours

`tool-showkase` declares the browser runtime as `compileOnly`, so it never contributes a Compose
version to your dependency resolution. Your app supplies `com.airbnb.android:showkase` to the
variants that need it. If a variant gets the tool without the runtime, tapping the tool logs an
error to logcat under the `Sentinel` tag instead of crashing.

## Minified builds

No extra configuration is needed: `tool-showkase` ships consumer keep rules
([`consumer-rules.pro`](consumer-rules.pro)) for your `@ShowkaseRoot` class and the generated code
Showkase resolves by name.

## Troubleshooting

**Tapping the tool does nothing.** Check logcat under the `Sentinel` tag. The variant has
`tool-showkase` but is missing `com.airbnb.android:showkase`.

**The browser opens but the catalogue is empty.** The processor did not run for this variant,
no class is annotated with `@ShowkaseRoot`, or nothing is annotated for Showkase to pick up.
