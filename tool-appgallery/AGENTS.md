# tool-appgallery

This module opens the host app's Huawei AppGallery listing. Namespace `com.infinum.sentinel.tool.appgallery`.

- `src/main/kotlin/com/infinum/sentinel/ui/tools/AppGalleryTool.kt`, `AppGalleryTool(appId, listener = default)` with `@JvmOverloads`. `appId` is the Huawei console app ID.
  - It resolves `appmarket://details?id=<pkg>` and launches `com.huawei.appmarket` directly if that is installed.
  - Otherwise it falls back to `https://appgallery.cloud.huawei.com/marketshare/app/C<appId>`.
- The title is the module's own `sentinel_app_gallery` string.
- Dependencies: `api(libs.library)` only.
- This module mirrors `tool-googleplay`, so apply logic fixes to both.
- The release stub lives in `sentinel-no-op/.../ui/tools/AppGalleryTool.kt`, where `appId` defaults to `""`.
- There are no tests. Verify with `./gradlew :tool-appgallery:lintRelease :tool-appgallery:detekt :tool-appgallery:apiCheck`.
