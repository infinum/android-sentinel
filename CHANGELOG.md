Change Log
==========

## Version 2.0.0

_2026-02-13_

* Bump minSdk to 24
* Bump compileSdk to 36
* Bump dependencies to latest stable versions
* New Sentinel tool: Network Emulator tool (OkHttp) - for modifying network conditions
* Fix duplicated namespace warnings


## Version 1.5.1

_2025-03-31_

* Hotfix: add targetedPreferences property to no-op

## Version 1.5.0

_2025-03-11_

* Add feature to show only predefined preferences

## Version 1.4.2

_2025-01-24_

* Bump compileSdk and targetSdk to 35
* Bump Chucker to version 4.1.0
* Bump AGP to 8.7.3
* Support for edge to edge

## Version 1.4.1

_2024-08-21_

* Hotfix for can't install multiple variants of app with Sentinel

## Version 1.4.0

_2024-07-18_

* UX improvements for SharedPreferences tool 
* Improvement for exported logs - datetime now in human-readable format 
* Fix WorkManager initialization bug that could crash apps

## Version 1.3.3

_2024-05-20_

* Update dependencies
* Fix AGP 8.3.0 support (duplicated resources resolved)
* Remove desugaring 
  * due to AGP changes 
  * CertificateTool is now only supported on devices running API 26 or newer

## Version 1.3.2

_2024-03-06_

* Fixed Android 13 notification permissions
* Fixed triggers activating even though they were disabled by the user in manifest

## Version 1.3.1

_2023-08-28_

* Fix log file provider authorities in code.

## Version 1.3.0

_2023-07-21_

* Fix log file provider authorities.

## Version 1.2.9

_2023-07-18_

* Update dependencies.
* Persist logs to disk. 

## Version 1.2.8

_2023-05-28_

* Fix a bug in BroadcastReceiverTrigger for USB and Airplane mode triggers.
* Update dependencies.

## Version 1.2.7

_2023-04-07_

* Add metadata to no-op.

## Version 1.2.6

_2023-04-04_

* Add initializer for easy no-op implementation purposes.

## Version 1.2.5

_2023-04-04_

* Change initializer visibility for chained initialization.

## Version 1.2.4

_2023-04-03_

* Replace Koin with Kotlin Inject.
* Filter out non Bundle keys in Bundle size monitor.
* Add support for explicit trigger declarations in manifest.

## Version 1.2.3

_2023-03-21_

* Update Kotlin to 1.8.10.
* Make shake trigger editable for transportation testing purposes.
* Disable auto start of Bundle monitor.
* Add package installer information in application data.
* Fix serialization of log entries in Timber tool.
* Fix concurrency of log entries in Timber tool.
* Update Gradle wrapper.
* Update dependencies.

## Version 1.2.2

_2022-11-15_

* Update Kotlin to 1.7.21.
* Fix Android 13 compatibility issues.

## Version 1.2.1

_2022-08-05_

* Update Kotlin to 1.7.10.
* Add Material3 design.

## Version 1.2.0

_2022-03-19_

* Implement tag filtering in Timber tool.
* Update Gradle wrapper.
* Update dependencies.

## Version 1.1.9

_2022-01-14_

* Implement screen information in Device tab.
* Implement font scale information in Device tab.
* Implement Proximity sensor trigger.
* Add Timber tool.
* Add Certificates tool.

## Version 1.1.8

_2022-01-06_

* Implement root check row in Device tab.
* Implement crash monitor for uncaught exceptions and ANRs.

## Version 1.1.7

_2021-12-31_

* Update Kotlin to 1.6.10.
* Update Gradle wrapper to 7.3.3.
* Implement Android 12 compatibility.
* Implement copy to clipboard for all values.
* Implement preferences editor.

## Version 1.1.6

_2021-08-23_

* Update Kotlin to 1.5.21.
* Fix R8 collisions on obfuscated class names.
* Add Huawei AppGallery tool.
* Add LeakCanary tool.

## Version 1.1.5

_2021-07-09_

* Update Kotlin to 1.5.20.

## Version 1.1.4

_2021-05-24_

* Update Kotlin to 1.5.10.
* Prepare for Android 12.

## Version 1.0.8

_2021-02-12_

* Fix shake trigger regression bug.

## Version 1.0.7

_2021-02-11_

* Update dependencies to stable versions.

## Version 1.0.6

_2021-02-09_

* Move project to MavenCentral.

## Version 1.0.5

_2021-01-29_

 * Enable Kotlin explicit mode.
 * Fix crash on Lifecycle 2.3.0
 * Update Chucker.
 * Update DbInspector.
 * Update Collar.

## Version 1.0.4

_2020-09-04_

 * Add no op tool stubs.
 * Update Chucker.

## Version 1.0.3

_2020-09-04_

 * Replace ContentProvider with AndroidX Startup Initializer.
 * Update Build tools 30.0.2.
 * Update Gradle to 6.6.1.
 * Update compile and target SDK to 30.

## Version 1.0.2

_2020-08-15_

 * Update Kotlin to 1.4.0.
 * Update Gradle to 6.6.
 * Update dependencies.
 * Promote settings screen to full screen Activity.
 * Replace BottomAppBar with BottomNavigationView.
 * Replace Executors with Coroutines.
 * Cleanup resources.
 * Implement build.properties.

## Version 1.0.1

_2020-07-06_

 * Update Gradle.
 * Update dependencies.
 * Add no-op tool classes to no-op package.

## Version 1.0.0

_2020-04-20_

 * Initial public release.