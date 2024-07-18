![Download](https://img.shields.io/maven-central/v/com.infinum.sentinel/sentinel) ![Validate Gradle Wrapper](https://github.com/infinum/android-sentinel/actions/workflows/gradle-wrapper-validation.yml/badge.svg) ![Code analysis](https://github.com/infinum/android-sentinel/actions/workflows/code-analysis.yml/badge.svg) [![CodeFactor](https://www.codefactor.io/repository/github/infinum/android-sentinel/badge/master)](https://www.codefactor.io/repository/github/infinum/android-sentinel/overview/master)

### <img align="left" src="logo.svg" width="48">

# Sentinel

![UI](ui.png)

_Sentinel_ is a simple one screen UI that provides standardised entry point for tools used in
development and QA alongside device, application and permissions data.  
It's designed to be easily configured and expanded depending on needs and requirements of developers
and QA testers.

The project is organized in the following modules:

- `sentinel` - contains a single screen UI that provides visual information about device,
  application, permissions and tools
- `sentinel-no-op` - contains stubs for easy release implementation of UI package and any Tools
  included
- `tool-chucker` - contains a class wrapper for Chucker
- `tool-collar` - contains a class wrapper for Collar
- `tool-dbinspector` - contains a class wrapper for DbInspector
- `tool-leakcanary` - contains a class wrapper for LeakCanary
- `tool-appgallery` - contains a class wrapper for Huawei AppGallery
- `tool-googleplay` - contains a class wrapper for Google Play
- `tool-thimble` - contains a class wrapper for Thimble
- `tool-timber` - contains a class wrapper for Timber
- `sample` - a sample app for testing and developing

## Usage

To include _Sentinel_ in your project, you have to add buildscript dependencies in your project
level `build.gradle` or `build.gradle.kts`:

**Groovy**

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
}
```

**KotlinDSL**

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
}
```

Then add the following dependencies in your app `build.gradle` or `build.gradle.kts` :

**Groovy**

```groovy
def sentinelVersion = "1.4.0"
debugImplementation "com.infinum.sentinel:sentinel:$sentinelVersion"
releaseImplementation "com.infinum.sentinel:sentinel-no-op:$sentinelVersion"
```

**KotlinDSL**

```kotlin
val sentinelVersion = "1.4.0"
debugImplementation("com.infinum.sentinel:sentinel:$sentinelVersion")
releaseImplementation("com.infinum.sentinel:sentinel-no-op:$sentinelVersion")
```

Basic tools are provided inside the main package but depending on requirements you might want to add
specific tools:

**Groovy**

```groovy
debugImplementation "com.infinum.sentinel:tool-chucker:$sentinelVersion"
debugImplementation "com.infinum.sentinel:tool-collar:$sentinelVersion"
debugImplementation "com.infinum.sentinel:tool-dbinspector:$sentinelVersion"
debugImplementation "com.infinum.sentinel:tool-leakcanary:$sentinelVersion"
debugImplementation "com.infinum.sentinel:tool-appgallery:$sentinelVersion"
debugImplementation "com.infinum.sentinel:tool-googleplay:$sentinelVersion"
debugImplementation "com.infinum.sentinel:tool-thimble:$sentinelVersion"
debugImplementation "com.infinum.sentinel:tool-timber:$sentinelVersion"
```

**KotlinDSL**

```kotlin
debugImplementation("com.infinum.sentinel:tool-chucker:$sentinelVersion")
debugImplementation("com.infinum.sentinel:tool-collar:$sentinelVersion")
debugImplementation("com.infinum.sentinel:tool-dbinspector:$sentinelVersion")
debugImplementation("com.infinum.sentinel:tool-leakcanary:$sentinelVersion")
debugImplementation("com.infinum.sentinel:tool-appgallery:$sentinelVersion")
debugImplementation("com.infinum.sentinel:tool-googleplay:$sentinelVersion")
debugImplementation("com.infinum.sentinel:tool-thimble:$sentinelVersion")
debugImplementation("com.infinum.sentinel:tool-timber:$sentinelVersion")
```

Now you can sync your project.

### Getting started

Create or inject an instance of _Sentinel_ in your Application class and start watching for
triggers:

```kotlin
Sentinel.watch(
    setOf(
        ChuckerTool(),
        CollarTool(),
        DbInspectorTool(),
        LeakCanaryTool(),
        AppGalleryTool(appId = "123456789"),
        GooglePlayTool(),
        ThimbleTool(),
        TimberTool(allowedTags = listOf("EXAMPLE")),
        CertificateTool(userCertificates = listOf())
    )
)
```

A set of tools should be provided as a _watch_ parameter. This set of tools can be empty.

### Tools

_Sentinel_ provides several different levels of tools for a developer to implement.

#### Built in

- `AppInfoTool` - opens Android OS Settings page of the application in which _Sentinel_ was
  implemented
- `CrashMonitorTool` - monitors exceptions and ANR crashes of the application in which _Sentinel_
  was implemented
- `BundleMonitorTool` - monitors Bundle objects passed around the application mostly for size
  avoiding _TransactionTooLarge_ exceptions
- `Preference Editor` - by tapping any preference _Sentinel_ opens a screen where you can edit
  current value
- `CertificateTool` - opens a list of system and user provided X.509 certificates with details    

#### Dependency wrappers

Depending of what you include as module dependencies, very specific tools are provided.

- `ChuckerTool` - a wrapper class that opens [Chucker](https://github.com/ChuckerTeam/chucker)
- `CollarTool` - a wrapper class that opens [Collar](https://github.com/infinum/android-collar)
- `DbInspectorTool` - a wrapper class that
  opens [DbInspector](https://github.com/infinum/android_dbinspector)
- `LeakCanaryTool` - a wrapper class that opens [LeakCanary](https://github.com/square/leakcanary)
- `ThimbleTool` - a wrapper class that opens [Thimble](https://github.com/infinum/android-thimble)
- `TimberTool` - a wrapper class that opens [Timber](https://github.com/JakeWharton/timber) . Takes in an optional list of allowed tags for filtering logged messages per tag.
- `AppGalleryTool` - a wrapper class that opens Huawei AppGallery of a published application or a
  web page of the application if Huawei AppGallery is not found
- `GooglePlayTool` - a wrapper class that opens Google Play of a published application or a web page
  of the application if Google Play is not found

#### Source abstractions

If you want to implement a different tool other than already packaged with a predefined type and
name, several are available.

- `NetworkTool` - a wrapper interface with a name *Network* for any network interceptors
- `MemoryTool` - a wrapper interface with a name *Memory* for any memory management tools
- `AnalyticsTool` - a wrapper interface with a name *Analytics* for any analytics collectors
- `DatabaseTool` - a wrapper interface with a name *Database* for any database viewers
- `ReportTool` - a wrapper interface with a name *Report* for any crash reporting tools
- `BluetoothTool` - a wrapper interface with a name *Bluetooth* for any Bluetooth loggers
- `DistributionTool` - a wrapper interface with a name *Distribution* for any release distribution
  channels
- `DesignTool` - a wrapper interface with a name *Design* for any design utilities

#### Independent implementations

An interface is provided named _Sentinel.Tool_ that requires implementation of a *String resource*
for a name and a _View.OnClickListener_. An optional icon *Drawable resource* can be supplied.
Implementing this interface enables any class to be provided as a tool in _Sentinel_.

```kotlin
interface Tool {

    @DrawableRes
    fun icon(): Int? = null

    @StringRes
    fun name(): Int

    fun listener(): View.OnClickListener
}
```

### Triggers

_Sentinel_ observes several different trigger events, determining when to show up.
*Manual* trigger cannot be turned off but rest are configurable through _Sentinel_
settings except *Foreground* trigger when running on emulators. Trigger states will be persisted
between sessions.
*Upon first run, all triggers are enabled.*  
Only way to override default trigger behaviour is to explicitly set them in you application manifest.  
If declared, these will override any changes that user does on each application launch.  
Accepted values are *0* and *1* to disable or enable a trigger.  
Anything else will result in the same way like the metadata key isn't declared at all.
```xml
<application
    android:name=".SampleApplication"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/AppTheme">
  
    ...
  
    <meta-data
        android:name="com.infinum.sentinel.trigger.shake"
        android:value="1" />

    <meta-data
        android:name="com.infinum.sentinel.trigger.proximity"
        android:value="0" />

    <meta-data
        android:name="com.infinum.sentinel.trigger.foreground"
        android:value="0" />

    <meta-data
        android:name="com.infinum.sentinel.trigger.usb_connected"
        android:value="0" />

    <meta-data
        android:name="com.infinum.sentinel.trigger.airplane_mode_on"
        android:value="0" />

    ...
  
</application>
```

- `Manual` - used for manually triggering UI with _show()_
- `Shake` - default trigger to show UI, shake device to invoke
- `Proximity` - shows UI every time sensor detects _near_ state  
- `Foreground` - shows UI every time application goes into foreground
- `USB connected` - shows UI every time an USB cable is plugged in
- `Airplane mode on` - shows UI every time Airplane mode is turned on

### Formatters

Data gathered and presented by _Sentinel_ can be shared to any text compliant recipient
applications.
_Sentinel_ provides a few simple text formatters for easy integrations into other systems.
_Plain_ formatter is selected by default, but selecting any other is persisted between sessions.

- `Plain`
- `Markdown`
- `JSON`
- `XML`
- `HTML`

## Crash monitor

_Sentinel_ has a built in default uncaught exception handler and ANR observer. If switched on in
settings, it will notify both in a form of a notification. Note that from Android 13 you need to give permission 
to the app to show notifications.
Once tapped on this notification, a screen with details is shown. A complete list of crashes is
persisted between sessions and available on demand.    
Methods to react on these crashes in a graceful way are provided in _Sentinel_.

```kotlin
Sentinel.setExceptionHandler { _, exception ->
    println("Exception happened: ${exception.message}")
    exitProcess(exception.hashCode())
}

Sentinel.setAnrListener { exception ->
    println("ANR happened: ${exception.message}")
    exitProcess(exception.hashCode())
}
```

## Bundle monitor

_Sentinel_ monitors Bundle objects passed around the application mostly for size limit of 500kB,
thus avoiding _TransactionTooLarge_ exceptions.  
In settings there are options to notify inside a running session, toggle specific variants or set
the limit of Bundle size.  
This monitor feature runs automatically for every _Activity_ but if you wish to disable monitoring
for a specific screen, you can do so in the _AndroidManifest.xml_ by adding a _meta-data_ node.
```xml
<activity
    android:name="com.example.ui.main.MainActivity"
    android:exported="false">
    <meta-data
      android:name="@string/sentinel_infinum_monitored"
      android:value="false" />
</activity>
```

## Requirements

This plugin has been written in Kotlin but works both inside Kotlin and Java projects. Minimum
required API level to use _Sentinel_ is *21* known
as [Android 5.0, Lollipop](https://www.android.com/versions/lollipop-5-0/).
_Sentinel_ is built with and for AndroidX projects.

## Contributing

Feedback and code contributions are very much welcome. Just make a pull request with a short
description of your changes. By making contributions to this project you give permission for your
code to be used under the same [license](LICENSE). For easier developing a `sample` application with
proper implementations is provided. If you wish to add a new specific dependency wrapper tool,
create a new module and set it up like the ones already provided. Then create a pull request.

## License

```
Copyright 2020 Infinum

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Credits
Maintained and sponsored by [Infinum](http://www.infinum.com).

<p align="center">
  <a href='https://infinum.com'>
    <picture>
        <source srcset="https://assets.infinum.com/brand/logo/static/white.svg" media="(prefers-color-scheme: dark)">
        <img src="https://assets.infinum.com/brand/logo/static/default.svg">
    </picture>
  </a>
</p>
