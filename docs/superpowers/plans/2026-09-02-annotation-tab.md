# Annotation Capture and Claude Export Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a developer tap any View or composable in the running app, attach a free-text note to it, and send the collected notes to a Claude Code routine that opens a pull request with the fixes.

**Architecture:** A `Sentinel.Tool` opens an annotations screen. Arming capture attaches an overlay `View` to the resumed Activity's decor view. Taps are hit-tested against the View hierarchy, or against the Compose `Group` tree when the hit lands inside an `AndroidComposeView`. Notes live in an in-memory cache and are shipped through a pluggable `AnnotationExporter` — clipboard by default, or an HTTP POST to a Claude Code routine.

**Tech Stack:** Kotlin, Android Views with ViewBinding, kotlin-inject, kotlinx.serialization, coroutines, Material Components, JUnit4, Espresso. Compose support via `compileOnly` reflection.

**Spec:** `docs/superpowers/specs/2026-09-02-annotation-tab-design.md`

## Global Constraints

- Module `sentinel`, package root `com.infinum.sentinel`, `namespace = "com.infinum.sentinel"`.
- `minSdk = 24`, `compileSdk = 37`, JVM toolchain 11 (`config.gradle.kts:8-9`).
- `-Xexplicit-api=strict`: every public declaration needs an explicit visibility modifier and an explicit return type. Everything not part of the documented public API must be `internal`.
- `resourcePrefix = "sentinel_"`: every new resource id, string, layout, drawable, style and dimen name must start with `sentinel_`.
- Detekt max line length is **140** (`config/detekt.yml:304`). `LongParameterList` thresholds: 6 for functions, 7 for constructors. `TooManyFunctions`: 22 per file and per class.
- Internal classes carry `@RestrictTo(RestrictTo.Scope.LIBRARY)` when they are Activities, Fragments or other entry points, matching the existing code.
- Unit tests are JUnit4: `org.junit.Test`, `org.junit.Assert.*`, `internal class NameTest`, backtick test names.
- No new **runtime** dependency is permitted in `sentinel`. Compose artifacts are `compileOnly` only. HTTP uses `java.net.HttpURLConnection`.
- Every public type added to `sentinel` must get a matching stub in `sentinel-no-op`, and `./gradlew apiDump` must be re-run so the checked-in `sentinel/api/sentinel.api` and `sentinel-no-op/api/sentinel-no-op.api` stay current.
- Do not `git push`. Commit locally only.

## File Structure

**Created in `sentinel/src/main/kotlin/com/infinum/sentinel/`:**

| Path | Responsibility |
|---|---|
| `data/models/local/annotations/Annotation.kt` | Public immutable note value type plus its export DTO |
| `data/models/local/annotations/AnnotationToolkit.kt` | `VIEW` / `COMPOSE` enum |
| `data/models/local/annotations/AnnotationTarget.kt` | Internal resolved target: live handle, bounds, locators |
| `data/sources/memory/annotations/AnnotationCache.kt` | Cache interface |
| `data/sources/memory/annotations/InMemoryAnnotationCache.kt` | List, ids, capture flag, sent marking |
| `domain/annotations/AnnotationExporter.kt` | Public exporter strategy |
| `domain/annotations/ClipboardAnnotationExporter.kt` | Default exporter |
| `domain/annotations/ClaudeRoutineExporter.kt` | Claude routine POST |
| `domain/annotations/HttpPoster.kt` | Thin seam over `HttpURLConnection` so the exporter is testable |
| `ui/annotations/overlay/TargetResolver.kt` | Resolver interface |
| `ui/annotations/overlay/AnnotatableViews.kt` | Annotatable filter, ordinal comparator, deepest-hit selection — pure functions |
| `ui/annotations/overlay/ViewTargetResolver.kt` | View hierarchy resolver |
| `ui/annotations/overlay/ComposeSupport.kt` | Reflection seam: is Compose present, fetch `Group` tree and `SemanticsOwner` |
| `ui/annotations/overlay/ComposeTargetResolver.kt` | Compose resolver built on the seam |
| `ui/annotations/overlay/AnnotationOverlayView.kt` | Marker drawing and touch state machine |
| `ui/annotations/overlay/AnnotationOverlayController.kt` | Activity lifecycle attachment |
| `ui/annotations/overlay/AnnotationNoteDialog.kt` | Note input bottom sheet |
| `ui/annotations/AnnotationsActivity.kt` … `AnnotationsAdapter.kt` | The screen |
| `ui/tools/AnnotationsTool.kt` | Public tool entry |

**Modified:** `di/component/DataComponent.kt`, `di/component/DomainComponent.kt`, `di/component/ViewModelComponent.kt`, `di/LibraryComponents.kt`, `sentinel/src/main/AndroidManifest.xml`, `sentinel/build.gradle.kts`, `gradle/libs.versions.toml`, `README.md`, `CHANGELOG.md`.

**Created in `sentinel-no-op`:** stubs mirroring every new public type.

---

### Task 1: Annotation model and export payload

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations/AnnotationToolkit.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations/Annotation.kt`
- Create: `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations/AnnotationToolkit.kt`
- Create: `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations/Annotation.kt`
- Test: `sentinel/src/test/kotlin/com/infinum/sentinel/data/models/local/annotations/AnnotationTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `public data class Annotation(...)` with `public fun toExportJson(): String` and `public companion object { public fun toExportJson(annotations: List<Annotation>): String }`; `public enum class AnnotationToolkit { VIEW, COMPOSE }`.

- [ ] **Step 1: Write the failing test**

Create `sentinel/src/test/kotlin/com/infinum/sentinel/data/models/local/annotations/AnnotationTest.kt`:

```kotlin
package com.infinum.sentinel.data.models.local.annotations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AnnotationTest {
    private fun viewAnnotation() =
        Annotation(
            id = "a3k9x2m1",
            note = "button too small",
            toolkit = AnnotationToolkit.VIEW,
            targetType = "MaterialButton",
            targetPath = "CoordinatorLayout>MaterialCardView>MaterialButton",
            viewId = "submitButton",
            testTag = null,
            sourceLocation = null,
            ordinal = 1,
            ordinalCount = 3,
            text = "Submit",
            screen = "MainActivity",
            fragment = "CheckoutFragment",
            route = null,
        )

    @Test
    fun `view annotation serializes every locator field`() {
        val expected =
            """
            {"id":"a3k9x2m1","note":"button too small","toolkit":"view","targetType":"MaterialButton",""" +
                """"targetPath":"CoordinatorLayout>MaterialCardView>MaterialButton","viewId":"submitButton",""" +
                """"testTag":null,"sourceLocation":null,"ordinal":1,"ordinalCount":3,"text":"Submit",""" +
                """"screen":"MainActivity","fragment":"CheckoutFragment","route":null}"""

        val actual = viewAnnotation().toExportJson()

        assertEquals(expected, actual)
    }

    @Test
    fun `compose annotation carries composable name and source location`() {
        val annotation =
            viewAnnotation().copy(
                toolkit = AnnotationToolkit.COMPOSE,
                targetType = "PrimaryButton",
                viewId = null,
                testTag = "submit_button",
                sourceLocation = "CheckoutScreen.kt:142",
            )

        val actual = annotation.toExportJson()

        assertTrue(actual.contains(""""toolkit":"compose""""))
        assertTrue(actual.contains(""""sourceLocation":"CheckoutScreen.kt:142""""))
        assertTrue(actual.contains(""""testTag":"submit_button""""))
        assertTrue(actual.contains(""""viewId":null"""))
    }

    @Test
    fun `a list serializes as a json array`() {
        val actual = Annotation.toExportJson(listOf(viewAnnotation(), viewAnnotation().copy(id = "b4l0y3n2")))

        assertTrue(actual.startsWith("[{"))
        assertTrue(actual.endsWith("}]"))
        assertTrue(actual.contains(""""id":"b4l0y3n2""""))
    }

    @Test
    fun `an empty list serializes as an empty json array`() {
        assertEquals("[]", Annotation.toExportJson(emptyList()))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "com.infinum.sentinel.data.models.local.annotations.AnnotationTest"`
Expected: FAIL — `Unresolved reference: Annotation` / `AnnotationToolkit`.

- [ ] **Step 3: Write the enum**

Create `AnnotationToolkit.kt`:

```kotlin
package com.infinum.sentinel.data.models.local.annotations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The UI toolkit an annotated element belongs to.
 */
@Serializable
public enum class AnnotationToolkit {
    @SerialName("view")
    VIEW,

    @SerialName("compose")
    COMPOSE,
}
```

- [ ] **Step 4: Write the model**

Create `Annotation.kt`. `target` is `@Transient` so the live handle never serializes, and it is `internal` so it stays out of the public API.

```kotlin
package com.infinum.sentinel.data.models.local.annotations

import kotlinx.serialization.EncodeDefaults
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

/**
 * An immutable note attached to an element captured on the running app.
 *
 * Every field except [target] is part of the export payload a Claude Code routine reads
 * to locate the element in source.
 */
@Suppress("LongParameterList")
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@EncodeDefaults
public data class Annotation(
    /** Session-unique id assigned by the cache. Links a fix back to its note. */
    public val id: String,
    /** The free-text note the user typed. */
    public val note: String,
    /** Which toolkit the target belongs to. */
    public val toolkit: AnnotationToolkit,
    /** View class simple name, or the composable function name. */
    public val targetType: String,
    /** Ancestor chain, broadest first, e.g. `CoordinatorLayout>MaterialCardView>MaterialButton`. */
    public val targetPath: String,
    /** Resource entry name of the view id, greppable as `R.id.x` and `@+id/x`. Null for Compose. */
    public val viewId: String? = null,
    /** `Modifier.testTag` value when set. Null for Views. */
    public val testTag: String? = null,
    /** `file:line` of the composable. Null for Views and when tooling data is unavailable. */
    public val sourceLocation: String? = null,
    /** 1-based position among same-type elements on the screen, top to bottom then left to right. */
    public val ordinal: Int = 1,
    /** Total count of same-type elements on the screen — the `M` in "N of M". */
    public val ordinalCount: Int = 1,
    /** Visible text or content description, used to disambiguate identical siblings. */
    public val text: String? = null,
    /** Simple name of the Activity the target lives in. */
    public val screen: String? = null,
    /** Simple name of the deepest Fragment containing the target. */
    public val fragment: String? = null,
    /** Navigation destination route when androidx-navigation is present. */
    public val route: String? = null,
    /** Whether this note has already been exported. */
    public val isSent: Boolean = false,
) {
    /** Live handle to the annotated element. Never serialized, never public. */
    @Transient
    internal var target: Any? = null

    public fun toExportJson(): String = json.encodeToString(serializer(), this)

    public companion object {
        private val json = Json { encodeDefaults = true }

        public fun toExportJson(annotations: List<Annotation>): String =
            json.encodeToString(ListSerializer, annotations)

        private val ListSerializer = kotlinx.serialization.builtins.ListSerializer(serializer())
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "com.infinum.sentinel.data.models.local.annotations.AnnotationTest"`
Expected: PASS, 4 tests.

If the field order in the first assertion does not match, reorder the assertion string to match declaration order rather than reordering the constructor — kotlinx.serialization emits fields in declaration order.

- [ ] **Step 6: Add the no-op stubs**

Create `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations/AnnotationToolkit.kt`:

```kotlin
package com.infinum.sentinel.data.models.local.annotations

public enum class AnnotationToolkit {
    VIEW,
    COMPOSE,
}
```

Create `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations/Annotation.kt` with the identical constructor signature and a `toExportJson` returning `""`:

```kotlin
package com.infinum.sentinel.data.models.local.annotations

@Suppress("LongParameterList", "unused")
public data class Annotation(
    public val id: String,
    public val note: String,
    public val toolkit: AnnotationToolkit,
    public val targetType: String,
    public val targetPath: String,
    public val viewId: String? = null,
    public val testTag: String? = null,
    public val sourceLocation: String? = null,
    public val ordinal: Int = 1,
    public val ordinalCount: Int = 1,
    public val text: String? = null,
    public val screen: String? = null,
    public val fragment: String? = null,
    public val route: String? = null,
    public val isSent: Boolean = false,
) {
    public fun toExportJson(): String = ""

    public companion object {
        @Suppress("UNUSED_PARAMETER")
        public fun toExportJson(annotations: List<Annotation>): String = ""
    }
}
```

- [ ] **Step 7: Refresh the API dumps and verify the build**

Run: `./gradlew :sentinel:apiDump :sentinel-no-op:apiDump && ./gradlew :sentinel:detekt`
Expected: both dumps updated, detekt clean.

- [ ] **Step 8: Commit**

```bash
git add sentinel/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations \
        sentinel/src/test/kotlin/com/infinum/sentinel/data/models/local/annotations \
        sentinel-no-op/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations \
        sentinel/api/sentinel.api sentinel-no-op/api/sentinel-no-op.api
git commit -m "feat: add annotation model and export payload"
```

---

### Task 2: Annotation cache

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/data/sources/memory/annotations/AnnotationCache.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/data/sources/memory/annotations/InMemoryAnnotationCache.kt`
- Modify: `sentinel/src/main/kotlin/com/infinum/sentinel/di/component/DataComponent.kt`
- Test: `sentinel/src/test/kotlin/com/infinum/sentinel/data/sources/memory/annotations/InMemoryAnnotationCacheTest.kt`

**Interfaces:**
- Consumes: `Annotation` from Task 1.
- Produces: `internal interface AnnotationCache` with `active: MutableStateFlow<Boolean>`, `load(): StateFlow<List<Annotation>>`, `suspend fun add(annotation: Annotation): Annotation`, `suspend fun updateNote(id: String, note: String)`, `suspend fun rebind(id: String, target: Any?)`, `suspend fun remove(id: String)`, `suspend fun markSent()`, `suspend fun clear()`. `DataComponent.annotationCache` exposes it.

- [ ] **Step 1: Write the failing test**

Create `InMemoryAnnotationCacheTest.kt`:

```kotlin
package com.infinum.sentinel.data.sources.memory.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class InMemoryAnnotationCacheTest {
    private fun annotation(note: String = "note") =
        Annotation(
            id = "",
            note = note,
            toolkit = AnnotationToolkit.VIEW,
            targetType = "MaterialButton",
            targetPath = "MaterialButton",
        )

    @Test
    fun `add assigns a non-empty id`() = runTest {
        val cache = InMemoryAnnotationCache()

        val stored = cache.add(annotation())

        assertTrue(stored.id.isNotEmpty())
        assertEquals(listOf(stored), cache.load().value)
    }

    @Test
    fun `add assigns unique ids`() = runTest {
        val cache = InMemoryAnnotationCache()

        val first = cache.add(annotation("first"))
        val second = cache.add(annotation("second"))

        assertNotEquals(first.id, second.id)
    }

    @Test
    fun `add keeps insertion order`() = runTest {
        val cache = InMemoryAnnotationCache()

        cache.add(annotation("first"))
        cache.add(annotation("second"))

        assertEquals(listOf("first", "second"), cache.load().value.map { it.note })
    }

    @Test
    fun `updateNote replaces text in place`() = runTest {
        val cache = InMemoryAnnotationCache()
        val stored = cache.add(annotation("before"))
        cache.add(annotation("other"))

        cache.updateNote(stored.id, "after")

        assertEquals(listOf("after", "other"), cache.load().value.map { it.note })
    }

    @Test
    fun `updateNote on an unknown id is a no-op`() = runTest {
        val cache = InMemoryAnnotationCache()
        cache.add(annotation("kept"))

        cache.updateNote("missing", "ignored")

        assertEquals(listOf("kept"), cache.load().value.map { it.note })
    }

    @Test
    fun `remove drops only the matching id`() = runTest {
        val cache = InMemoryAnnotationCache()
        val first = cache.add(annotation("first"))
        cache.add(annotation("second"))

        cache.remove(first.id)

        assertEquals(listOf("second"), cache.load().value.map { it.note })
    }

    @Test
    fun `identical notes are removed by id not by value`() = runTest {
        val cache = InMemoryAnnotationCache()
        val first = cache.add(annotation("same"))
        val second = cache.add(annotation("same"))

        cache.remove(second.id)

        assertEquals(listOf(first.id), cache.load().value.map { it.id })
    }

    @Test
    fun `markSent flags every unsent note`() = runTest {
        val cache = InMemoryAnnotationCache()
        cache.add(annotation("first"))
        cache.add(annotation("second"))

        cache.markSent()

        assertTrue(cache.load().value.all { it.isSent })
    }

    @Test
    fun `clear empties the cache`() = runTest {
        val cache = InMemoryAnnotationCache()
        cache.add(annotation())

        cache.clear()

        assertTrue(cache.load().value.isEmpty())
    }

    @Test
    fun `capture flag starts inactive`() {
        assertFalse(InMemoryAnnotationCache().active.value)
    }
}
```

- [ ] **Step 2: Add the coroutines test dependency**

`kotlinx-coroutines-test` is required by `runTest`. Add to `gradle/libs.versions.toml` under `[libraries]`, reusing the existing `coroutines` version reference:

```toml
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }
```

Add to `sentinel/build.gradle.kts` in the `dependencies` block next to the other `testImplementation` lines:

```kotlin
    testImplementation(libs.coroutines.test)
```

- [ ] **Step 3: Run the test to verify it fails**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*InMemoryAnnotationCacheTest"`
Expected: FAIL — `Unresolved reference: InMemoryAnnotationCache`.

- [ ] **Step 4: Write the interface**

Create `AnnotationCache.kt`:

```kotlin
package com.infinum.sentinel.data.sources.memory.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal interface AnnotationCache {
    /** Single source of truth for whether capture mode is armed. */
    val active: MutableStateFlow<Boolean>

    fun load(): StateFlow<List<Annotation>>

    /** Stores [annotation], assigning an id when it has none. Returns the stored copy. */
    suspend fun add(annotation: Annotation): Annotation

    suspend fun updateNote(id: String, note: String)

    /** Re-points the note at a freshly created element after navigating back to its screen. */
    suspend fun rebind(id: String, target: Any?)

    suspend fun remove(id: String)

    suspend fun markSent()

    suspend fun clear()
}
```

- [ ] **Step 5: Write the implementation**

Create `InMemoryAnnotationCache.kt`:

```kotlin
package com.infinum.sentinel.data.sources.memory.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject

@Inject
internal class InMemoryAnnotationCache : AnnotationCache {
    private companion object {
        private const val ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789"
        private const val ID_LENGTH = 8
    }

    private val annotations = mutableListOf<Annotation>()
    private val annotationsFlow = MutableStateFlow<List<Annotation>>(emptyList())

    override val active = MutableStateFlow(false)

    override fun load(): StateFlow<List<Annotation>> = annotationsFlow.asStateFlow()

    override suspend fun add(annotation: Annotation): Annotation {
        val stored = if (annotation.id.isEmpty()) annotation.copy(id = generateId()) else annotation
        stored.target = annotation.target
        annotations.add(stored)
        publish()
        return stored
    }

    override suspend fun updateNote(id: String, note: String) =
        replace(id) { it.copy(note = note) }

    override suspend fun rebind(id: String, target: Any?) {
        val index = annotations.indexOfFirst { it.id == id }
        if (index < 0) return
        annotations[index].target = target
        publish()
    }

    override suspend fun remove(id: String) {
        if (annotations.removeAll { it.id == id }) publish()
    }

    override suspend fun markSent() {
        var changed = false
        annotations.forEachIndexed { index, annotation ->
            if (!annotation.isSent) {
                val sent = annotation.copy(isSent = true)
                sent.target = annotation.target
                annotations[index] = sent
                changed = true
            }
        }
        if (changed) publish()
    }

    override suspend fun clear() {
        annotations.clear()
        publish()
    }

    private fun replace(id: String, transform: (Annotation) -> Annotation) {
        val index = annotations.indexOfFirst { it.id == id }
        if (index < 0) return
        val previous = annotations[index]
        val updated = transform(previous)
        updated.target = previous.target
        annotations[index] = updated
        publish()
    }

    private fun publish() {
        annotationsFlow.value = annotations.toList()
    }

    /** Short opaque id, unique among the notes currently stored. Nothing parses its shape. */
    private fun generateId(): String {
        var candidate: String
        do {
            candidate = (1..ID_LENGTH).map { ALPHABET[Random.nextInt(ALPHABET.length)] }.joinToString("")
        } while (annotations.any { it.id == candidate })
        return candidate
    }
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*InMemoryAnnotationCacheTest"`
Expected: PASS, 10 tests.

- [ ] **Step 7: Wire the cache into DataComponent**

In `sentinel/src/main/kotlin/com/infinum/sentinel/di/component/DataComponent.kt`, add the imports next to the existing `bundles` cache imports:

```kotlin
import com.infinum.sentinel.data.sources.memory.annotations.AnnotationCache
import com.infinum.sentinel.data.sources.memory.annotations.InMemoryAnnotationCache
```

Add the abstract property next to `abstract val bundlesCache: BundlesCache` (around line 260):

```kotlin
    abstract val annotationCache: AnnotationCache
```

Add the provider next to `fun bundlesCache()` (around line 304):

```kotlin
    @Provides
    @DataScope
    fun annotationCache(): AnnotationCache = InMemoryAnnotationCache()
```

- [ ] **Step 8: Verify the component still compiles**

Run: `./gradlew :sentinel:compileDebugKotlin`
Expected: BUILD SUCCESSFUL. kotlin-inject code generation runs via KSP; a missing binding fails here.

- [ ] **Step 9: Commit**

```bash
git add sentinel/src/main/kotlin/com/infinum/sentinel/data/sources/memory/annotations \
        sentinel/src/test/kotlin/com/infinum/sentinel/data/sources/memory/annotations \
        sentinel/src/main/kotlin/com/infinum/sentinel/di/component/DataComponent.kt \
        sentinel/build.gradle.kts gradle/libs.versions.toml
git commit -m "feat: add in-memory annotation cache"
```

---

### Task 3: Exporter strategy and clipboard exporter

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/domain/annotations/AnnotationExporter.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/domain/annotations/ClipboardAnnotationExporter.kt`
- Create: `sentinel/src/main/res/drawable/sentinel_ic_copy.xml`
- Create: `sentinel/src/main/res/drawable/sentinel_ic_send.xml`
- Modify: `sentinel/src/main/res/values/strings.xml`
- Create: `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/domain/annotations/AnnotationExporter.kt`
- Create: `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/domain/annotations/ClipboardAnnotationExporter.kt`

**Interfaces:**
- Consumes: `Annotation` from Task 1.
- Produces: `public interface AnnotationExporter { @DrawableRes public fun icon(): Int; @StringRes public fun label(): Int; public suspend fun export(context: Context, annotations: List<Annotation>): String }` and `public class ClipboardAnnotationExporter : AnnotationExporter`.

`export` takes a `Context` because a clipboard write needs one and the screen must not have to inject it for third-party exporters. Borealis had ambient access to one; Android does not.

- [ ] **Step 1: Write the interface**

Create `AnnotationExporter.kt`:

```kotlin
package com.infinum.sentinel.domain.annotations

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.local.annotations.Annotation

/**
 * Strategy for sending captured annotations somewhere.
 *
 * Implement this to route notes to a server, a file or an issue tracker. The full
 * [Annotation] objects are passed in so an implementation controls its own serialization;
 * [Annotation.toExportJson] produces the default payload.
 *
 * Return a short status message to show the user. Throw to signal failure — the screen
 * reports it as `Export failed: ...`. Implementations stay UI free.
 */
public interface AnnotationExporter {
    /** Drawable resource for the export button's icon. */
    @DrawableRes
    public fun icon(): Int

    /** String resource for the export button's label. */
    @StringRes
    public fun label(): Int

    public suspend fun export(context: Context, annotations: List<Annotation>): String
}
```

- [ ] **Step 2: Add the strings**

Append to `sentinel/src/main/res/values/strings.xml` inside `<resources>`:

```xml
    <string name="sentinel_annotations">Annotations</string>
    <string name="sentinel_annotations_copy">Copy to clipboard</string>
    <string name="sentinel_annotations_send">Send to Claude</string>
    <string name="sentinel_annotations_copied">Copied %1$d annotations</string>
    <string name="sentinel_annotations_sent">Sent %1$d annotations</string>
    <string name="sentinel_annotations_enable">Enable annotations</string>
    <string name="sentinel_annotations_start">Start annotating</string>
    <string name="sentinel_annotations_hint">Tap an element on the current screen to attach a note.</string>
    <string name="sentinel_annotations_empty_export">No annotations to export</string>
    <string name="sentinel_annotations_export_failed">Export failed: %1$s</string>
    <string name="sentinel_annotations_sent_label">Sent</string>
    <string name="sentinel_annotations_delete_title">Delete annotation?</string>
    <string name="sentinel_annotations_delete_message">This will remove annotation %1$d.</string>
    <string name="sentinel_annotations_note_new">Note for %1$s</string>
    <string name="sentinel_annotations_note_edit">Edit note for %1$s</string>
    <string name="sentinel_annotations_note_hint">Describe the issue here</string>
    <string name="sentinel_annotations_add_note">Add note</string>
    <string name="sentinel_annotations_stop">Stop annotating</string>
    <string name="sentinel_annotations_missing_token">No Claude token configured</string>
    <string name="sentinel_annotations_compose_unsupported">Compose version not supported: source locations unavailable</string>
    <string name="sentinel_cancel">Cancel</string>
    <string name="sentinel_save">Save</string>
    <string name="sentinel_delete">Delete</string>
```

- [ ] **Step 3: Add the icons**

Create `sentinel/src/main/res/drawable/sentinel_ic_copy.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M16,1H4C2.9,1 2,1.9 2,3v14h2V3h12V1zM19,5H8C6.9,5 6,5.9 6,7v14c0,1.1 0.9,2 2,2h11c1.1,0 2,-0.9 2,-2V7C21,5.9 20.1,5 19,5zM19,21H8V7h11V21z" />
</vector>
```

Create `sentinel/src/main/res/drawable/sentinel_ic_send.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M2.01,21L23,12 2.01,3 2,10l15,2 -15,2z" />
</vector>
```

- [ ] **Step 4: Write the clipboard exporter**

Create `ClipboardAnnotationExporter.kt`:

```kotlin
package com.infinum.sentinel.domain.annotations

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.annotations.Annotation

/**
 * Default exporter. Copies the export JSON to the system clipboard.
 */
public class ClipboardAnnotationExporter : AnnotationExporter {
    private companion object {
        private const val CLIP_LABEL = "Sentinel annotations"
    }

    override fun icon(): Int = R.drawable.sentinel_ic_copy

    override fun label(): Int = R.string.sentinel_annotations_copy

    override suspend fun export(context: Context, annotations: List<Annotation>): String {
        val json = Annotation.toExportJson(annotations)
        context.getSystemService<ClipboardManager>()
            ?.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, json))
        return context.getString(R.string.sentinel_annotations_copied, annotations.size)
    }
}
```

- [ ] **Step 5: Verify it compiles**

Run: `./gradlew :sentinel:compileDebugKotlin :sentinel:detekt`
Expected: BUILD SUCCESSFUL, detekt clean.

- [ ] **Step 6: Add the no-op stubs**

Create `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/domain/annotations/AnnotationExporter.kt`:

```kotlin
package com.infinum.sentinel.domain.annotations

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.infinum.sentinel.data.models.local.annotations.Annotation

public interface AnnotationExporter {
    @DrawableRes
    public fun icon(): Int

    @StringRes
    public fun label(): Int

    public suspend fun export(context: Context, annotations: List<Annotation>): String
}
```

Create `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/domain/annotations/ClipboardAnnotationExporter.kt`:

```kotlin
package com.infinum.sentinel.domain.annotations

import android.content.Context
import com.infinum.sentinel.data.models.local.annotations.Annotation

@Suppress("unused")
public class ClipboardAnnotationExporter : AnnotationExporter {
    override fun icon(): Int = 0

    override fun label(): Int = 0

    @Suppress("UNUSED_PARAMETER")
    override suspend fun export(context: Context, annotations: List<Annotation>): String = ""
}
```

- [ ] **Step 7: Refresh the API dumps**

Run: `./gradlew :sentinel:apiDump :sentinel-no-op:apiDump && ./gradlew compareSentinelApis`
Expected: dumps updated, `✅ API is identical between Sentinel and Sentinel no-op!`

- [ ] **Step 8: Commit**

```bash
git add sentinel/src/main/kotlin/com/infinum/sentinel/domain/annotations \
        sentinel/src/main/res sentinel-no-op/src/main/kotlin/com/infinum/sentinel/domain/annotations \
        sentinel/api/sentinel.api sentinel-no-op/api/sentinel-no-op.api
git commit -m "feat: add annotation exporter strategy and clipboard exporter"
```

---

### Task 4: Claude routine exporter

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/domain/annotations/HttpPoster.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/domain/annotations/ClaudeRoutineExporter.kt`
- Modify: `sentinel/src/main/AndroidManifest.xml:4`
- Create: `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/domain/annotations/ClaudeRoutineExporter.kt`
- Test: `sentinel/src/test/kotlin/com/infinum/sentinel/domain/annotations/ClaudeRoutineExporterTest.kt`

**Interfaces:**
- Consumes: `AnnotationExporter`, `Annotation`.
- Produces: `public class ClaudeRoutineExporter(token: () -> String?, fireUrl: String) : AnnotationExporter`. Internal seam `internal fun interface HttpPoster { suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse }` and `internal data class HttpResponse(val code: Int, val body: String)`.

The `HttpPoster` seam exists so the exporter's header assembly, payload shape and error handling are unit-testable without a network.

- [ ] **Step 1: Write the failing test**

Create `ClaudeRoutineExporterTest.kt`:

```kotlin
package com.infinum.sentinel.domain.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

internal class ClaudeRoutineExporterTest {
    private val fireUrl = "https://api.anthropic.com/v1/claude_code/routines/trig_test/fire"

    private val annotations =
        listOf(
            Annotation(
                id = "a3k9x2m1",
                note = "button too small",
                toolkit = AnnotationToolkit.VIEW,
                targetType = "MaterialButton",
                targetPath = "MaterialButton",
            ),
        )

    private class RecordingPoster(private val response: HttpResponse) : HttpPoster {
        var url: String? = null
        var headers: Map<String, String> = emptyMap()
        var body: String? = null
        var calls: Int = 0

        override suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse {
            this.url = url
            this.headers = headers
            this.body = body
            this.calls++
            return response
        }
    }

    @Test
    fun `sends the annotation json wrapped in a text field`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ "sk-ant-test" }, fireUrl, poster)

        exporter.send(annotations)

        assertEquals(fireUrl, poster.url)
        assertTrue(poster.body!!.startsWith("""{"text":"["""))
        assertTrue(poster.body!!.contains("button too small"))
    }

    @Test
    fun `sends the documented authorization and beta headers`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ "sk-ant-test" }, fireUrl, poster)

        exporter.send(annotations)

        assertEquals("Bearer sk-ant-test", poster.headers["Authorization"])
        assertEquals("2023-06-01", poster.headers["anthropic-version"])
        assertEquals("experimental-cc-routine-2026-04-01", poster.headers["anthropic-beta"])
        assertEquals("application/json", poster.headers["Content-Type"])
    }

    @Test
    fun `reports how many annotations were sent`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ "sk-ant-test" }, fireUrl, poster)

        assertEquals(1, exporter.send(annotations))
    }

    @Test
    fun `a blank token fails without making a request`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ "   " }, fireUrl, poster)

        try {
            exporter.send(annotations)
            fail("Expected an IllegalStateException")
        } catch (expected: IllegalStateException) {
            assertEquals(0, poster.calls)
        }
    }

    @Test
    fun `a null token fails without making a request`() = runTest {
        val poster = RecordingPoster(HttpResponse(200, ""))
        val exporter = ClaudeRoutineExporter({ null }, fireUrl, poster)

        try {
            exporter.send(annotations)
            fail("Expected an IllegalStateException")
        } catch (expected: IllegalStateException) {
            assertEquals(0, poster.calls)
        }
    }

    @Test
    fun `a non 2xx response throws with the status and body`() = runTest {
        val poster = RecordingPoster(HttpResponse(401, """{"error":"unauthorized"}"""))
        val exporter = ClaudeRoutineExporter({ "sk-ant-test" }, fireUrl, poster)

        try {
            exporter.send(annotations)
            fail("Expected an IllegalStateException")
        } catch (expected: IllegalStateException) {
            assertTrue(expected.message!!.contains("401"))
            assertTrue(expected.message!!.contains("unauthorized"))
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*ClaudeRoutineExporterTest"`
Expected: FAIL — `Unresolved reference: ClaudeRoutineExporter`.

- [ ] **Step 3: Write the HTTP seam**

Create `HttpPoster.kt`:

```kotlin
package com.infinum.sentinel.domain.annotations

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class HttpResponse(
    val code: Int,
    val body: String,
)

/**
 * Minimal POST seam. Exists so exporters can be tested without a network, and so
 * Sentinel needs no HTTP client dependency.
 */
internal fun interface HttpPoster {
    suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse
}

internal class UrlConnectionHttpPoster : HttpPoster {
    private companion object {
        private const val CONNECT_TIMEOUT_MILLIS = 15_000
        private const val READ_TIMEOUT_MILLIS = 30_000
    }

    override suspend fun post(url: String, headers: Map<String, String>, body: String): HttpResponse =
        withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
                connection.readTimeout = READ_TIMEOUT_MILLIS
                connection.doOutput = true
                headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }

                connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseBody = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
                HttpResponse(code, responseBody)
            } finally {
                connection.disconnect()
            }
        }
}
```

- [ ] **Step 4: Write the exporter**

Create `ClaudeRoutineExporter.kt`. `send` carries the testable logic and returns the count; `export` wraps it with the Context-dependent message.

```kotlin
package com.infinum.sentinel.domain.annotations

import android.content.Context
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.annotations.Annotation
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Fires a Claude Code routine, sending the annotation JSON as an extra turn appended to
 * the routine's session.
 *
 * [token] supplies a Claude Code OAuth token, used as a bearer token. It is a lambda rather
 * than a string so an app can read it from `BuildConfig` or a development-only source instead
 * of hardcoding a credential that ships inside the APK. [fireUrl] is the routine's fire
 * endpoint, for example
 * `https://api.anthropic.com/v1/claude_code/routines/<trigger-id>/fire`.
 */
public class ClaudeRoutineExporter internal constructor(
    private val token: () -> String?,
    private val fireUrl: String,
    private val poster: HttpPoster,
) : AnnotationExporter {
    public constructor(token: () -> String?, fireUrl: String) : this(token, fireUrl, UrlConnectionHttpPoster())

    private companion object {
        private const val ANTHROPIC_VERSION = "2023-06-01"
        private const val ANTHROPIC_BETA = "experimental-cc-routine-2026-04-01"
        private const val SUCCESS_RANGE_START = 200
        private const val SUCCESS_RANGE_END = 299
    }

    override fun icon(): Int = R.drawable.sentinel_ic_send

    override fun label(): Int = R.string.sentinel_annotations_send

    override suspend fun export(context: Context, annotations: List<Annotation>): String {
        val count = send(annotations)
        return context.getString(R.string.sentinel_annotations_sent, count)
    }

    /** Posts [annotations] and returns how many were sent. Throws [IllegalStateException] on failure. */
    internal suspend fun send(annotations: List<Annotation>): Int {
        val bearer = token()
        check(!bearer.isNullOrBlank()) { "No Claude token configured" }

        val payload =
            Json.encodeToString(
                JsonObject.serializer(),
                JsonObject(mapOf("text" to JsonPrimitive(Annotation.toExportJson(annotations)))),
            )

        val response =
            poster.post(
                url = fireUrl,
                headers =
                    mapOf(
                        "Authorization" to "Bearer $bearer",
                        "anthropic-version" to ANTHROPIC_VERSION,
                        "anthropic-beta" to ANTHROPIC_BETA,
                        "Content-Type" to "application/json",
                    ),
                body = payload,
            )

        check(response.code in SUCCESS_RANGE_START..SUCCESS_RANGE_END) {
            "Failed to send annotations to Claude (HTTP ${response.code}): ${response.body}"
        }

        return annotations.size
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*ClaudeRoutineExporterTest"`
Expected: PASS, 6 tests.

- [ ] **Step 6: Add the INTERNET permission**

In `sentinel/src/main/AndroidManifest.xml`, directly after line 4's `POST_NOTIFICATIONS` declaration:

```xml
    <uses-permission android:name="android.permission.INTERNET"/>
```

This merges into the host app. Sentinel is a `debugImplementation` dependency and `sentinel-no-op` is used for release builds, so release manifests are unaffected. This is documented in Task 14.

- [ ] **Step 7: Add the no-op stub**

Create `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/domain/annotations/ClaudeRoutineExporter.kt`:

```kotlin
package com.infinum.sentinel.domain.annotations

import android.content.Context
import com.infinum.sentinel.data.models.local.annotations.Annotation

@Suppress("unused", "UNUSED_PARAMETER")
public class ClaudeRoutineExporter(
    token: () -> String?,
    fireUrl: String,
) : AnnotationExporter {
    override fun icon(): Int = 0

    override fun label(): Int = 0

    override suspend fun export(context: Context, annotations: List<Annotation>): String = ""
}
```

- [ ] **Step 8: Refresh the API dumps and verify**

Run: `./gradlew :sentinel:apiDump :sentinel-no-op:apiDump && ./gradlew compareSentinelApis :sentinel:detekt`
Expected: dumps updated, API identical, detekt clean.

Confirm `sentinel/api/sentinel.api` shows the two-argument public constructor and does **not** show the three-argument internal one.

- [ ] **Step 9: Commit**

```bash
git add sentinel/src/main/kotlin/com/infinum/sentinel/domain/annotations \
        sentinel/src/test/kotlin/com/infinum/sentinel/domain/annotations \
        sentinel/src/main/AndroidManifest.xml \
        sentinel-no-op/src/main/kotlin/com/infinum/sentinel/domain/annotations \
        sentinel/api/sentinel.api sentinel-no-op/api/sentinel-no-op.api
git commit -m "feat: add Claude routine annotation exporter"
```

---

### Task 5: Toolkit-independent target geometry and selection rules

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/Bounds.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/TargetNode.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/TargetSelection.kt`
- Test: `sentinel/src/test/kotlin/com/infinum/sentinel/ui/annotations/overlay/TargetSelectionTest.kt`

**Interfaces:**
- Consumes: nothing.
- Produces: `internal data class Bounds(left, top, right, bottom)`; `internal interface TargetNode { type, viewId, testTag, sourceLocation, text, annotatable, bounds, children, parent }`; `internal object TargetSelection` with `deepestAt(root, x, y): TargetNode?`, `nearestAnnotatable(node): TargetNode?`, `stepUp(node): TargetNode?`, `pathOf(node): String`, `ordinalOf(root, node): Pair<Int, Int>`.

Both resolvers adapt their own tree to `TargetNode`, so every selection rule is written once and unit-tested on the JVM. Note this deliberately avoids `android.graphics.Rect`: unit tests here run against the unmocked `android.jar`, where its methods throw.

- [ ] **Step 1: Write the failing test**

Create `TargetSelectionTest.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class TargetSelectionTest {
    private class FakeNode(
        override val type: String,
        override val bounds: Bounds,
        override val annotatable: Boolean = true,
        override val viewId: String? = null,
        override val testTag: String? = null,
        override val sourceLocation: String? = null,
        override val text: String? = null,
        children: List<FakeNode> = emptyList(),
    ) : TargetNode {
        override var parent: TargetNode? = null
        override val children: List<TargetNode> = children

        init {
            children.forEach { it.parent = this }
        }
    }

    @Test
    fun `deepestAt returns the innermost node containing the point`() {
        val inner = FakeNode("Button", Bounds(10, 10, 50, 50))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(inner))

        assertEquals(inner, TargetSelection.deepestAt(root, 20, 20))
    }

    @Test
    fun `deepestAt returns the root when no child contains the point`() {
        val inner = FakeNode("Button", Bounds(10, 10, 50, 50))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(inner))

        assertEquals(root, TargetSelection.deepestAt(root, 80, 80))
    }

    @Test
    fun `deepestAt returns null when the point is outside the root`() {
        val root = FakeNode("Root", Bounds(0, 0, 100, 100))

        assertNull(TargetSelection.deepestAt(root, 200, 200))
    }

    @Test
    fun `deepestAt prefers the last overlapping sibling because it draws on top`() {
        val below = FakeNode("Below", Bounds(0, 0, 50, 50))
        val above = FakeNode("Above", Bounds(0, 0, 50, 50))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(below, above))

        assertEquals(above, TargetSelection.deepestAt(root, 10, 10))
    }

    @Test
    fun `nearestAnnotatable walks up past non annotatable nodes`() {
        val leaf = FakeNode("Leaf", Bounds(10, 10, 20, 20), annotatable = false)
        val middle = FakeNode("Middle", Bounds(5, 5, 30, 30), annotatable = false, children = listOf(leaf))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(middle))

        assertEquals(root, TargetSelection.nearestAnnotatable(leaf))
    }

    @Test
    fun `nearestAnnotatable returns the node itself when it qualifies`() {
        val leaf = FakeNode("Leaf", Bounds(10, 10, 20, 20))
        FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(leaf))

        assertEquals(leaf, TargetSelection.nearestAnnotatable(leaf))
    }

    @Test
    fun `stepUp broadens to the next annotatable ancestor`() {
        val leaf = FakeNode("Leaf", Bounds(10, 10, 20, 20))
        val middle = FakeNode("Middle", Bounds(5, 5, 30, 30), annotatable = false, children = listOf(leaf))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(middle))

        assertEquals(root, TargetSelection.stepUp(leaf))
    }

    @Test
    fun `stepUp returns null at the outermost annotatable node so the caller can wrap`() {
        val root = FakeNode("Root", Bounds(0, 0, 100, 100))

        assertNull(TargetSelection.stepUp(root))
    }

    @Test
    fun `pathOf joins the chain broadest first`() {
        val leaf = FakeNode("Icon", Bounds(10, 10, 20, 20))
        val middle = FakeNode("Button", Bounds(5, 5, 30, 30), children = listOf(leaf))
        FakeNode("Card", Bounds(0, 0, 100, 100), children = listOf(middle))

        assertEquals("Card>Button>Icon", TargetSelection.pathOf(leaf))
    }

    @Test
    fun `ordinalOf counts same type nodes top to bottom then left to right`() {
        val topLeft = FakeNode("Chip", Bounds(0, 0, 10, 10))
        val topRight = FakeNode("Chip", Bounds(20, 0, 30, 10))
        val bottom = FakeNode("Chip", Bounds(0, 40, 10, 50))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(bottom, topRight, topLeft))

        assertEquals(1 to 3, TargetSelection.ordinalOf(root, topLeft))
        assertEquals(2 to 3, TargetSelection.ordinalOf(root, topRight))
        assertEquals(3 to 3, TargetSelection.ordinalOf(root, bottom))
    }

    @Test
    fun `ordinalOf reports one of one for a lone node`() {
        val only = FakeNode("Chip", Bounds(0, 0, 10, 10))
        val root = FakeNode("Root", Bounds(0, 0, 100, 100), children = listOf(only))

        assertEquals(1 to 1, TargetSelection.ordinalOf(root, only))
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*TargetSelectionTest"`
Expected: FAIL — `Unresolved reference: Bounds`.

- [ ] **Step 3: Write Bounds**

Create `Bounds.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

/**
 * Screen-space rectangle in pixels. Deliberately not `android.graphics.Rect` so the
 * selection rules stay unit-testable on the JVM.
 */
internal data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val isEmpty: Boolean get() = width <= 0 || height <= 0
    val centerX: Int get() = left + width / 2
    val centerY: Int get() = top + height / 2

    fun contains(x: Int, y: Int): Boolean = x >= left && x < right && y >= top && y < bottom

    fun intersects(other: Bounds): Boolean =
        left < other.right && other.left < right && top < other.bottom && other.top < bottom
}
```

- [ ] **Step 4: Write TargetNode**

Create `TargetNode.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

/**
 * A toolkit-independent view of one annotatable element.
 *
 * `ViewTargetResolver` adapts an Android `View` to this, `ComposeTargetResolver` adapts a
 * Compose tooling `Group`. Every selection rule in [TargetSelection] is written against this
 * interface so it is shared by both and testable without a device.
 *
 * [children] must be ordered back to front — the last child draws on top.
 */
internal interface TargetNode {
    /** View class simple name, or the composable function name. */
    val type: String

    /** Resource entry name of the view id, when the element has one. */
    val viewId: String?

    /** `Modifier.testTag` value, when set. */
    val testTag: String?

    /** `file:line` of the composable, when tooling data resolved it. */
    val sourceLocation: String?

    /** Visible text or content description. */
    val text: String?

    /** Whether this element is worth selecting, as opposed to framework scaffolding. */
    val annotatable: Boolean

    val bounds: Bounds

    val children: List<TargetNode>

    val parent: TargetNode?
}
```

- [ ] **Step 5: Write the selection rules**

Create `TargetSelection.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

/**
 * The rules that turn a tap into a selection. Shared by both resolvers.
 */
internal object TargetSelection {
    /**
     * The innermost node containing ([x], [y]), or `null` when the point misses [root].
     * Children are tested last-first because the last child draws on top.
     */
    fun deepestAt(root: TargetNode, x: Int, y: Int): TargetNode? {
        if (!root.bounds.contains(x, y)) return null
        for (index in root.children.indices.reversed()) {
            val hit = deepestAt(root.children[index], x, y)
            if (hit != null) return hit
        }
        return root
    }

    /** The deepest annotatable node at or above [node], or `null` when none qualifies. */
    fun nearestAnnotatable(node: TargetNode): TargetNode? {
        var current: TargetNode? = node
        while (current != null) {
            if (current.annotatable) return current
            current = current.parent
        }
        return null
    }

    /**
     * The selection broadened one annotatable node toward the root, or `null` when [node] is
     * already the outermost one — the caller then wraps back to the deepest target.
     */
    fun stepUp(node: TargetNode): TargetNode? {
        var current = node.parent
        while (current != null) {
            if (current.annotatable) return current
            current = current.parent
        }
        return null
    }

    /** The chain of annotatable types from the outermost ancestor down to [node]. */
    fun pathOf(node: TargetNode): String {
        val types = mutableListOf<String>()
        var current: TargetNode? = node
        while (current != null) {
            if (current.annotatable) types.add(current.type)
            current = current.parent
        }
        return types.reversed().joinToString(">")
    }

    /**
     * Position of [node] among same-type nodes under [root], as `(ordinal, count)`, ordered top
     * to bottom then left to right. `(1, count)` when [node] cannot be located among the matches.
     */
    fun ordinalOf(root: TargetNode, node: TargetNode): Pair<Int, Int> {
        val matches = mutableListOf<TargetNode>()
        collectByType(root, node.type, matches)
        matches.sortWith(compareBy({ it.bounds.top }, { it.bounds.left }))

        val index = matches.indexOfFirst { it === node }
        if (index < 0) return 1 to matches.size.coerceAtLeast(1)
        return index + 1 to matches.size
    }

    private fun collectByType(node: TargetNode, type: String, into: MutableList<TargetNode>) {
        if (node.type == type && !node.bounds.isEmpty) into.add(node)
        node.children.forEach { collectByType(it, type, into) }
    }
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*TargetSelectionTest"`
Expected: PASS, 11 tests.

- [ ] **Step 7: Commit**

```bash
git add sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay \
        sentinel/src/test/kotlin/com/infinum/sentinel/ui/annotations/overlay
git commit -m "feat: add toolkit-independent target selection rules"
```

---

### Task 6: View hierarchy resolver

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/ViewTargetNode.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/TargetResolver.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/ViewTargetResolver.kt`
- Test: `sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay/ViewTargetResolverTest.kt`

**Interfaces:**
- Consumes: `TargetNode`, `TargetSelection`, `Bounds`, `Annotation`.
- Produces: `internal interface TargetResolver { fun resolve(root: View, x: Int, y: Int): ResolvedTarget?; fun rebind(annotation: Annotation, root: View): ResolvedTarget? }` and `internal data class ResolvedTarget(node, handle, toolkit)`. `internal class ViewTargetResolver(...) : TargetResolver`. `internal fun View.toTargetNode(parent: TargetNode?): ViewTargetNode`.

- [ ] **Step 1: Write the failing instrumented test**

Create `sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay/ViewTargetResolverTest.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ViewTargetResolverTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    private fun laidOut(root: FrameLayout, width: Int = 200, height: Int = 200): FrameLayout {
        root.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY),
        )
        root.layout(0, 0, width, height)
        return root
    }

    @Test
    fun `a text view is annotatable and reports its text`() {
        val text = TextView(context).apply { setText("Submit") }
        val root = laidOut(FrameLayout(context).apply { addView(text) })

        val node = root.toTargetNode(parent = null)
        val leaf = TargetSelection.deepestAt(node, 10, 10)!!

        assertEquals("TextView", leaf.type)
        assertEquals("Submit", leaf.text)
        assertTrue(leaf.annotatable)
    }

    @Test
    fun `a plain container without an app id is not annotatable`() {
        val container = LinearLayout(context)
        val root = laidOut(FrameLayout(context).apply { addView(container) })

        val node = root.toTargetNode(parent = null)
        val child = node.children.first()

        assertEquals("LinearLayout", child.type)
        assertTrue(!child.annotatable)
    }

    @Test
    fun `a clickable view is annotatable`() {
        val clickable = FrameLayout(context).apply { isClickable = true }
        val root = laidOut(FrameLayout(context).apply { addView(clickable) })

        assertTrue(root.toTargetNode(parent = null).children.first().annotatable)
    }

    @Test
    fun `an invisible view is skipped`() {
        val hidden = TextView(context).apply { visibility = android.view.View.GONE }
        val root = laidOut(FrameLayout(context).apply { addView(hidden) })

        assertTrue(root.toTargetNode(parent = null).children.isEmpty())
    }

    @Test
    fun `resolve returns the button under the point with its path`() {
        val button = Button(context).apply { setText("Go") }
        val root = laidOut(FrameLayout(context).apply { addView(button) })

        val resolved = ViewTargetResolver().resolve(root, 5, 5)!!

        assertEquals("Button", resolved.node.type)
        assertTrue(resolved.handle === button)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :sentinel:connectedDebugAndroidTest --tests "*ViewTargetResolverTest"`
Expected: FAIL — `Unresolved reference: toTargetNode`. A connected device or emulator must be running.

- [ ] **Step 3: Write the resolver contract**

Create `TargetResolver.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit

/**
 * A resolved selection: the toolkit-independent description plus the live handle used to
 * recompute bounds while the note is on screen.
 */
internal data class ResolvedTarget(
    val node: TargetNode,
    val handle: Any,
    val toolkit: AnnotationToolkit,
)

internal interface TargetResolver {
    /** The annotatable target at ([x], [y]) in screen coordinates, or `null`. */
    fun resolve(root: View, x: Int, y: Int): ResolvedTarget?

    /** The live element [annotation] should now point at after its screen was recreated. */
    fun rebind(annotation: Annotation, root: View): ResolvedTarget?
}
```

- [ ] **Step 4: Write the View adapter**

Create `ViewTargetNode.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

/**
 * Adapts an Android [View] subtree to [TargetNode].
 *
 * Built eagerly on tap. Invisible and zero-size views are dropped, so what the tree contains
 * is what the user can actually see.
 */
internal class ViewTargetNode(
    val view: View,
    override val parent: TargetNode?,
) : TargetNode {
    override val type: String = view.javaClass.simpleName

    override val viewId: String? = resolveViewId(view)

    override val testTag: String? = null

    override val sourceLocation: String? = null

    override val text: String? =
        when {
            view is TextView && view.text.isNotEmpty() -> view.text.toString()
            view.contentDescription != null -> view.contentDescription.toString()
            else -> null
        }

    override val bounds: Bounds = boundsOf(view)

    /**
     * A view is worth selecting when the app declared its id, when it draws content, or when
     * it is interactive. Framework scaffolding is excluded outright.
     */
    override val annotatable: Boolean =
        !isFrameworkChrome(view) &&
            (
                viewId != null ||
                    view is TextView ||
                    view is ImageView ||
                    view is EditText ||
                    view is Checkable ||
                    view.isClickable
            )

    override val children: List<TargetNode> =
        if (view is ViewGroup) {
            (0 until view.childCount)
                .map { view.getChildAt(it) }
                .filter { it.visibility == View.VISIBLE && it.width > 0 && it.height > 0 }
                .map { ViewTargetNode(it, this) }
        } else {
            emptyList()
        }

    private companion object {
        private val CHROME_TYPES =
            setOf(
                "DecorView",
                "ActionBarOverlayLayout",
                "ContentFrameLayout",
                "ActionBarContainer",
                "ViewStubCompat",
                "FitWindowsLinearLayout",
                "AnnotationOverlayView",
            )

        private fun isFrameworkChrome(view: View): Boolean =
            view.javaClass.simpleName in CHROME_TYPES || view.javaClass.name.startsWith("com.android.internal")

        /**
         * The resource entry name of the view's id when the app declared it. A library's or the
         * framework's ids are ignored: only the app's own ids are greppable in the app's source.
         */
        private fun resolveViewId(view: View): String? {
            val id = view.id
            if (id == View.NO_ID) return null
            return try {
                if (view.resources.getResourcePackageName(id) != view.context.packageName) {
                    null
                } else {
                    view.resources.getResourceEntryName(id)
                }
            } catch (ignored: Resources.NotFoundException) {
                null
            }
        }

        private fun boundsOf(view: View): Bounds {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return Bounds(location[0], location[1], location[0] + view.width, location[1] + view.height)
        }
    }
}

internal fun View.toTargetNode(parent: TargetNode?): ViewTargetNode = ViewTargetNode(this, parent)
```

- [ ] **Step 5: Add the rebind anchor to the model**

`rebind` needs the captured centre point. Add two fields to `Annotation` in
`data/models/local/annotations/Annotation.kt`, directly after `route`, and mirror them in the
no-op stub:

```kotlin
    /** Screen x of the target's centre when captured. Anchors re-binding after navigation. */
    public val rebindX: Int = 0,
    /** Screen y of the target's centre when captured. Anchors re-binding after navigation. */
    public val rebindY: Int = 0,
```

Update `AnnotationTest`'s first assertion to include `,"rebindX":0,"rebindY":0` before `,"isSent":false` — run the test and copy the exact serialized string from the failure message rather than hand-writing it.

- [ ] **Step 6: Write the resolver**

Create `ViewTargetResolver.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit

internal class ViewTargetResolver : TargetResolver {
    override fun resolve(root: View, x: Int, y: Int): ResolvedTarget? {
        val tree = root.toTargetNode(parent = null)
        val deepest = TargetSelection.deepestAt(tree, x, y) ?: return null
        val selected = TargetSelection.nearestAnnotatable(deepest) ?: deepest
        return ResolvedTarget(selected, (selected as ViewTargetNode).view, AnnotationToolkit.VIEW)
    }

    /**
     * Re-points [annotation] at the element now painted where it was captured. Accepts only a
     * same-type match that also matches the captured id, or — when the element has no id — the
     * captured text. Fields that did not resolve on either side stay permissive, so a missing
     * value never orphans a note.
     */
    override fun rebind(annotation: Annotation, root: View): ResolvedTarget? {
        val candidate = resolve(root, annotation.rebindX, annotation.rebindY) ?: return null
        val node = candidate.node
        if (node.type != annotation.targetType) return null
        if (annotation.viewId != null && node.viewId != annotation.viewId) return null
        if (annotation.viewId == null && annotation.text != null && node.text != annotation.text) return null
        return candidate
    }
}
```

- [ ] **Step 7: Run the tests to verify they pass**

Run: `./gradlew :sentinel:testDebugUnitTest && ./gradlew :sentinel:connectedDebugAndroidTest --tests "*ViewTargetResolverTest"`
Expected: unit tests PASS; instrumented tests PASS, 5 tests.

- [ ] **Step 8: Refresh the API dumps and commit**

```bash
./gradlew :sentinel:apiDump :sentinel-no-op:apiDump && ./gradlew compareSentinelApis :sentinel:detekt
git add sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay \
        sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay \
        sentinel/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations/Annotation.kt \
        sentinel/src/test/kotlin/com/infinum/sentinel/data/models/local/annotations/AnnotationTest.kt \
        sentinel-no-op/src/main/kotlin/com/infinum/sentinel/data/models/local/annotations/Annotation.kt \
        sentinel/api/sentinel.api sentinel-no-op/api/sentinel-no-op.api
git commit -m "feat: resolve annotation targets in the view hierarchy"
```

---

### Task 7: Compose target resolver

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `sentinel/build.gradle.kts`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/ComposeSupport.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/ComposeTargetNode.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/ComposeTargetResolver.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/CompositeTargetResolver.kt`
- Test: `sentinel/src/test/kotlin/com/infinum/sentinel/ui/annotations/overlay/ComposeSourceLocationTest.kt`

**Interfaces:**
- Consumes: `TargetResolver`, `ResolvedTarget`, `TargetNode`, `Bounds`.
- Produces: `internal object ComposeSupport { val isAvailable: Boolean; fun isComposeHost(view: View): Boolean; fun rootNodeOf(view: View): TargetNode?; fun formatSourceLocation(sourceFile: String?, lineNumber: Int): String? }` and `internal class CompositeTargetResolver(...) : TargetResolver`.

The reflection stays inside `ComposeSupport` and is intentionally thin. Everything above it is ordinary code exercised by the shared `TargetSelection` tests.

- [ ] **Step 1: Add the compileOnly dependencies**

In `gradle/libs.versions.toml` under `[versions]`:

```toml
compose = "1.9.1"
```

Under `[libraries]`:

```toml
compose-ui = { module = "androidx.compose.ui:ui", version.ref = "compose" }
compose-ui-tooling-data = { module = "androidx.compose.ui:ui-tooling-data", version.ref = "compose" }
```

In `sentinel/build.gradle.kts`, in the `dependencies` block:

```kotlin
    compileOnly(libs.compose.ui)
    compileOnly(libs.compose.ui.tooling.data)
```

`compileOnly` means consumers that do not use Compose pull nothing, and the classes are absent at runtime — which is exactly what `ComposeSupport.isAvailable` guards against.

- [ ] **Step 2: Verify the dependencies resolve**

Run: `./gradlew :sentinel:dependencies --configuration debugCompileClasspath | grep compose`
Expected: both artifacts listed at 1.9.1. If 1.9.1 is unavailable, use the newest stable 1.9.x and update the version reference.

- [ ] **Step 3: Write the failing test for the location formatter**

Create `ComposeSourceLocationTest.kt`. This covers the one piece of Compose handling with real branching that does not need Compose on the classpath:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class ComposeSourceLocationTest {
    @Test
    fun `formats file and line`() {
        assertEquals("CheckoutScreen.kt:142", ComposeSupport.formatSourceLocation("CheckoutScreen.kt", 142))
    }

    @Test
    fun `strips a leading path so the value stays greppable`() {
        assertEquals(
            "CheckoutScreen.kt:142",
            ComposeSupport.formatSourceLocation("com/example/checkout/CheckoutScreen.kt", 142),
        )
    }

    @Test
    fun `returns null without a source file`() {
        assertNull(ComposeSupport.formatSourceLocation(null, 142))
    }

    @Test
    fun `returns null for a non positive line number`() {
        assertNull(ComposeSupport.formatSourceLocation("CheckoutScreen.kt", -1))
    }
}
```

- [ ] **Step 4: Run the test to verify it fails**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*ComposeSourceLocationTest"`
Expected: FAIL — `Unresolved reference: ComposeSupport`.

- [ ] **Step 5: Write the reflection seam**

Create `ComposeSupport.kt`. This is the Radiography recipe, narrowed to what the resolver needs.

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.util.SparseArray
import android.view.View
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Composition
import androidx.compose.ui.tooling.data.Group
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.compose.ui.tooling.data.asTree

/**
 * The reflection boundary for Compose support.
 *
 * Compose exposes no public API for reading a live composition, so this mirrors the approach
 * Square's Radiography uses: pull the [Composition] out of the host view's keyed tags, unwrap it
 * to its [Composer], and read the slot table through the tooling library's [asTree].
 *
 * All of this is version sensitive. Every entry point returns `null` rather than throwing when
 * Compose is missing or shaped differently than expected, and callers fall back to the View
 * resolver.
 */
internal object ComposeSupport {
    private const val ANDROID_COMPOSE_VIEW = "androidx.compose.ui.platform.AndroidComposeView"
    private const val WRAPPED_COMPOSITION = "androidx.compose.ui.platform.WrappedComposition"
    private const val COMPOSITION_IMPL = "androidx.compose.runtime.CompositionImpl"

    /** Whether Compose is on the runtime classpath at all. */
    val isAvailable: Boolean by lazy {
        try {
            Class.forName(ANDROID_COMPOSE_VIEW)
            true
        } catch (ignored: Throwable) {
            false
        }
    }

    /** Whether [view] is the private view type Compose uses to host a composition. */
    fun isComposeHost(view: View): Boolean =
        isAvailable && view.javaClass.name.contains("AndroidComposeView")

    /**
     * The composition under [view] adapted to [TargetNode], or `null` when the composition
     * cannot be read — an unsupported Compose version, or `ui-tooling-data` missing.
     */
    fun rootNodeOf(view: View): TargetNode? =
        try {
            groupTreeOf(view)?.let { ComposeTargetNode(it, parent = null) }
        } catch (ignored: LinkageError) {
            null
        } catch (ignored: ReflectiveOperationException) {
            null
        }

    /**
     * `file:line`, with any leading path stripped so the value matches what a developer would
     * grep for. `null` when either half is missing.
     */
    fun formatSourceLocation(sourceFile: String?, lineNumber: Int): String? {
        if (sourceFile.isNullOrBlank() || lineNumber <= 0) return null
        return "${sourceFile.substringAfterLast('/')}:$lineNumber"
    }

    @OptIn(UiToolingDataApi::class)
    private fun groupTreeOf(view: View): Group? {
        val composition = view.keyedTags().firstIsInstanceOrNull<Composition>() ?: return null
        val composer = composition.unwrap().composerOrNull() ?: return null
        return composer.compositionData.asTree()
    }

    private fun View.keyedTags(): SparseArray<*> {
        val field =
            try {
                View::class.java.getDeclaredField("mKeyedTags").apply { isAccessible = true }
            } catch (ignored: NoSuchFieldException) {
                return SparseArray<Nothing>(0)
            }
        return field.get(this) as? SparseArray<*> ?: SparseArray<Nothing>(0)
    }

    private inline fun <reified T> SparseArray<*>.firstIsInstanceOrNull(): T? {
        for (index in 0 until size()) {
            val value = valueAt(index)
            if (value is T) return value
        }
        return null
    }

    private fun Composition.unwrap(): Composition {
        if (this::class.java.name != WRAPPED_COMPOSITION) return this
        val field = Class.forName(WRAPPED_COMPOSITION).getDeclaredField("original").apply { isAccessible = true }
        return field.get(this) as Composition
    }

    private fun Composition.composerOrNull(): Composer? {
        if (this::class.java.name != COMPOSITION_IMPL) return null
        val field = Class.forName(COMPOSITION_IMPL).getDeclaredField("composer").apply { isAccessible = true }
        return field.get(this) as? Composer
    }
}
```

- [ ] **Step 6: Write the Compose adapter**

Create `ComposeTargetNode.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import androidx.compose.ui.tooling.data.Group
import androidx.compose.ui.tooling.data.UiToolingDataApi

/**
 * Adapts a Compose tooling [Group] to [TargetNode].
 *
 * A group is annotatable when it has both a name and a source location — that is, it is a
 * composable the developer wrote rather than an anonymous framework group.
 */
@OptIn(UiToolingDataApi::class)
internal class ComposeTargetNode(
    val group: Group,
    override val parent: TargetNode?,
) : TargetNode {
    override val type: String = group.name.orEmpty()

    override val viewId: String? = null

    override val testTag: String? =
        group.modifierInfo
            .firstNotNullOfOrNull { info -> TEST_TAG_REGEX.find(info.modifier.toString())?.groupValues?.get(1) }

    override val sourceLocation: String? =
        ComposeSupport.formatSourceLocation(group.location?.sourceFile, group.location?.lineNumber ?: -1)

    override val text: String? = group.data.filterIsInstance<String>().firstOrNull()

    override val bounds: Bounds =
        Bounds(group.box.left, group.box.top, group.box.right, group.box.bottom)

    override val annotatable: Boolean = type.isNotEmpty() && sourceLocation != null

    override val children: List<TargetNode> =
        group.children.filter { !it.box.isEmpty() }.map { ComposeTargetNode(it, this) }

    private companion object {
        private val TEST_TAG_REGEX = Regex("""testTag=([A-Za-z0-9_\-.]+)""")

        private fun androidx.compose.ui.unit.IntRect.isEmpty(): Boolean = width <= 0 || height <= 0
    }
}
```

- [ ] **Step 7: Write the Compose resolver and the composite**

Create `ComposeTargetResolver.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit

internal class ComposeTargetResolver : TargetResolver {
    override fun resolve(root: View, x: Int, y: Int): ResolvedTarget? {
        val tree = ComposeSupport.rootNodeOf(root) ?: return null
        val deepest = TargetSelection.deepestAt(tree, x, y) ?: return null
        val selected = TargetSelection.nearestAnnotatable(deepest) ?: return null
        return ResolvedTarget(selected, (selected as ComposeTargetNode).group, AnnotationToolkit.COMPOSE)
    }

    /**
     * Compose recreates its node objects on every recomposition, so a saved group handle is
     * never reusable. Re-binding therefore re-resolves at the captured point and accepts a match
     * on source location, which is stable across recomposition, or on the test tag.
     */
    override fun rebind(annotation: Annotation, root: View): ResolvedTarget? {
        val candidate = resolve(root, annotation.rebindX, annotation.rebindY) ?: return null
        val node = candidate.node
        if (node.type != annotation.targetType) return null
        if (annotation.sourceLocation != null && node.sourceLocation != annotation.sourceLocation) return null
        if (annotation.testTag != null && node.testTag != annotation.testTag) return null
        return candidate
    }
}
```

Create `CompositeTargetResolver.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.view.View
import android.view.ViewGroup
import com.infinum.sentinel.data.models.local.annotations.Annotation

/**
 * Routes a tap to the Compose resolver when it lands inside a composition, and to the View
 * resolver otherwise.
 *
 * When Compose is present but unreadable — an unsupported version, or `ui-tooling-data` absent —
 * the Compose resolver returns `null` and the View resolver still yields the hosting
 * `ComposeView`, so the element stays annotatable with no source location. [composeUnreadable]
 * records that so the screen can say so once.
 */
internal class CompositeTargetResolver(
    private val views: TargetResolver = ViewTargetResolver(),
    private val compose: TargetResolver = ComposeTargetResolver(),
) : TargetResolver {
    var composeUnreadable: Boolean = false
        private set

    override fun resolve(root: View, x: Int, y: Int): ResolvedTarget? {
        val host = composeHostAt(root, x, y)
        if (host != null) {
            val resolved = compose.resolve(host, x, y)
            if (resolved != null) return resolved
            composeUnreadable = true
        }
        return views.resolve(root, x, y)
    }

    override fun rebind(annotation: Annotation, root: View): ResolvedTarget? {
        val host = composeHostAt(root, annotation.rebindX, annotation.rebindY)
        if (host != null) {
            val rebound = compose.rebind(annotation, host)
            if (rebound != null) return rebound
        }
        return views.rebind(annotation, root)
    }

    /** The deepest Compose host view containing the point, or `null`. */
    private fun composeHostAt(root: View, x: Int, y: Int): View? {
        if (!ComposeSupport.isAvailable) return null
        if (!root.containsOnScreen(x, y)) return null
        if (root is ViewGroup) {
            for (index in root.childCount - 1 downTo 0) {
                val child = root.getChildAt(index)
                if (child.visibility != View.VISIBLE) continue
                val hit = composeHostAt(child, x, y)
                if (hit != null) return hit
            }
        }
        return if (ComposeSupport.isComposeHost(root)) root else null
    }

    private fun View.containsOnScreen(x: Int, y: Int): Boolean {
        val location = IntArray(2)
        getLocationOnScreen(location)
        return x >= location[0] && x < location[0] + width && y >= location[1] && y < location[1] + height
    }
}
```

- [ ] **Step 8: Run the tests to verify they pass**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*ComposeSourceLocationTest" && ./gradlew :sentinel:compileDebugKotlin :sentinel:detekt`
Expected: 4 tests PASS, compilation succeeds, detekt clean.

- [ ] **Step 9: Commit**

```bash
git add gradle/libs.versions.toml sentinel/build.gradle.kts \
        sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay \
        sentinel/src/test/kotlin/com/infinum/sentinel/ui/annotations/overlay
git commit -m "feat: resolve annotation targets in the compose tree"
```

---

### Task 8: Overlay view — marker drawing and touch states

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationMarker.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationOverlayView.kt`
- Modify: `sentinel/src/main/res/values/dimens.xml`
- Modify: `sentinel/src/main/res/values/colors.xml`
- Test: `sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationOverlayViewTest.kt`

**Interfaces:**
- Consumes: `Bounds`, `ResolvedTarget`, `TargetResolver`, `CompositeTargetResolver`, `Annotation`.
- Produces: `internal class AnnotationOverlayView(context: Context) : View(context)` with `var captureActive: Boolean`, `var markers: List<AnnotationMarker>`, `var onCaptureTap: ((x: Int, y: Int) -> Unit)?`, `var onBadgeTapped: ((Annotation) -> Unit)?`, `fun markerAt(x: Int, y: Int): AnnotationMarker?`. `internal data class AnnotationMarker(val number: Int, val bounds: Bounds, val annotation: Annotation)`.

- [ ] **Step 1: Write the failing instrumented test**

Create `AnnotationOverlayViewTest.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.content.Context
import android.os.SystemClock
import android.view.MotionEvent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AnnotationOverlayViewTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun annotation(id: String = "a1") =
        Annotation(
            id = id,
            note = "note",
            toolkit = AnnotationToolkit.VIEW,
            targetType = "Button",
            targetPath = "Button",
        )

    private fun tapAt(view: AnnotationOverlayView, x: Float, y: Float): Boolean {
        val now = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0)
        val handled = view.dispatchTouchEvent(down)
        down.recycle()
        return handled
    }

    @Test
    fun `capture mode consumes every touch`() {
        val view = AnnotationOverlayView(context).apply { captureActive = true }

        assertTrue(tapAt(view, 5f, 5f))
    }

    @Test
    fun `passive mode lets touches through`() {
        val view = AnnotationOverlayView(context).apply { captureActive = false }

        assertFalse(tapAt(view, 5f, 5f))
    }

    @Test
    fun `passive mode consumes a touch on a badge`() {
        val view =
            AnnotationOverlayView(context).apply {
                captureActive = false
                markers = listOf(AnnotationMarker(1, Bounds(0, 0, 100, 100), annotation()))
            }

        assertTrue(tapAt(view, 5f, 5f))
    }

    @Test
    fun `passive mode reports the tapped badge`() {
        var tapped: Annotation? = null
        val view =
            AnnotationOverlayView(context).apply {
                captureActive = false
                markers = listOf(AnnotationMarker(1, Bounds(0, 0, 100, 100), annotation("target")))
                onBadgeTapped = { tapped = it }
            }

        tapAt(view, 5f, 5f)

        assertEquals("target", tapped?.id)
    }

    @Test
    fun `markerAt misses a point outside the badge square`() {
        val view =
            AnnotationOverlayView(context).apply {
                markers = listOf(AnnotationMarker(1, Bounds(0, 0, 100, 100), annotation()))
            }

        assertNull(view.markerAt(90, 90))
    }

    @Test
    fun `markerAt prefers the most recently added badge when two overlap`() {
        val view =
            AnnotationOverlayView(context).apply {
                markers =
                    listOf(
                        AnnotationMarker(1, Bounds(0, 0, 100, 100), annotation("older")),
                        AnnotationMarker(2, Bounds(0, 0, 100, 100), annotation("newer")),
                    )
            }

        assertEquals("newer", view.markerAt(5, 5)?.annotation?.id)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :sentinel:connectedDebugAndroidTest --tests "*AnnotationOverlayViewTest"`
Expected: FAIL — `Unresolved reference: AnnotationOverlayView`.

- [ ] **Step 3: Add the dimensions and colour**

Append to `sentinel/src/main/res/values/dimens.xml` inside `<resources>`:

```xml
    <dimen name="sentinel_annotation_badge_size">24dp</dimen>
    <dimen name="sentinel_annotation_outline_width">2dp</dimen>
    <dimen name="sentinel_annotation_badge_text_size">12sp</dimen>
```

Append to `sentinel/src/main/res/values/colors.xml` inside `<resources>`:

```xml
    <color name="sentinel_annotation_marker">#FFE53935</color>
```

- [ ] **Step 4: Write the marker type**

Create `AnnotationMarker.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import com.infinum.sentinel.data.models.local.annotations.Annotation

/**
 * A saved note resolved to its current on-screen position, ready to draw.
 *
 * [number] is the 1-based position in the cache, so a note keeps its number even while other
 * notes are hidden on another screen.
 */
internal data class AnnotationMarker(
    val number: Int,
    val bounds: Bounds,
    val annotation: Annotation,
)
```

- [ ] **Step 5: Write the overlay view**

Create `AnnotationOverlayView.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.annotations.Annotation

/**
 * The layer that draws saved notes over the running app and, while capture is armed, takes
 * every touch so a tap selects an element instead of operating the app.
 *
 * Markers are painted on one canvas rather than built as child views: this layer redraws
 * whenever the app scrolls, and a canvas pass is far cheaper than a view tree.
 */
@SuppressLint("ViewConstructor")
internal class AnnotationOverlayView(
    context: Context,
) : View(context) {
    private val badgeSize = resources.getDimensionPixelSize(R.dimen.sentinel_annotation_badge_size)
    private val markerColor = ContextCompat.getColor(context, R.color.sentinel_annotation_marker)

    private val outlinePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimension(R.dimen.sentinel_annotation_outline_width)
            color = markerColor
        }

    private val badgePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = markerColor
        }

    private val badgeTextPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = resources.getDimension(R.dimen.sentinel_annotation_badge_text_size)
            isFakeBoldText = true
        }

    private val selectionPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = resources.getDimension(R.dimen.sentinel_annotation_outline_width) * 2
            color = markerColor
        }

    /** While true this layer swallows every touch and taps select elements. */
    var captureActive: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    var markers: List<AnnotationMarker> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    /** Bounds of the element currently selected in capture mode. */
    var selection: Bounds? = null
        set(value) {
            field = value
            invalidate()
        }

    /** Called with the screen coordinates of a tap while capture is armed. */
    var onCaptureTap: ((x: Int, y: Int) -> Unit)? = null

    /** Called when a saved note's badge is tapped. */
    var onBadgeTapped: ((Annotation) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val origin = IntArray(2)
        getLocationOnScreen(origin)

        markers.forEach { marker ->
            val rect =
                RectF(
                    (marker.bounds.left - origin[0]).toFloat(),
                    (marker.bounds.top - origin[1]).toFloat(),
                    (marker.bounds.right - origin[0]).toFloat(),
                    (marker.bounds.bottom - origin[1]).toFloat(),
                )
            canvas.drawRect(rect, outlinePaint)
            canvas.drawRect(rect.left, rect.top, rect.left + badgeSize, rect.top + badgeSize, badgePaint)
            canvas.drawText(
                marker.number.toString(),
                rect.left + badgeSize / 2f,
                rect.top + badgeSize / 2f - (badgeTextPaint.ascent() + badgeTextPaint.descent()) / 2f,
                badgeTextPaint,
            )
        }

        selection?.let {
            canvas.drawRect(
                (it.left - origin[0]).toFloat(),
                (it.top - origin[1]).toFloat(),
                (it.right - origin[0]).toFloat(),
                (it.bottom - origin[1]).toFloat(),
                selectionPaint,
            )
        }
    }

    /**
     * Capture mode consumes everything. Passive mode consumes only a badge tap, so the app
     * underneath stays fully usable while saved notes remain editable.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val screen = IntArray(2)
        getLocationOnScreen(screen)
        val x = event.x.toInt() + screen[0]
        val y = event.y.toInt() + screen[1]

        if (!captureActive) {
            val marker = markerAt(x, y) ?: return false
            if (event.actionMasked == MotionEvent.ACTION_DOWN) onBadgeTapped?.invoke(marker.annotation)
            return true
        }

        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            val marker = markerAt(x, y)
            if (marker != null) {
                onBadgeTapped?.invoke(marker.annotation)
            } else {
                onCaptureTap?.invoke(x, y)
            }
        }
        return true
    }

    /**
     * The saved note whose badge square covers ([x], [y]) in screen coordinates, most recently
     * added first so the topmost badge wins.
     */
    fun markerAt(x: Int, y: Int): AnnotationMarker? =
        markers.lastOrNull { marker ->
            Bounds(
                marker.bounds.left,
                marker.bounds.top,
                marker.bounds.left + badgeSize,
                marker.bounds.top + badgeSize,
            ).contains(x, y)
        }
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew :sentinel:connectedDebugAndroidTest --tests "*AnnotationOverlayViewTest"`
Expected: PASS, 6 tests.

- [ ] **Step 7: Commit**

```bash
git add sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay \
        sentinel/src/main/res/values/dimens.xml sentinel/src/main/res/values/colors.xml \
        sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay
git commit -m "feat: draw annotation markers and route overlay touches"
```

---

### Task 9: Overlay controller and note input

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationNoteDialog.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationOverlayController.kt`
- Create: `sentinel/src/main/res/layout/sentinel_dialog_annotation_note.xml`
- Modify: `sentinel/src/main/kotlin/com/infinum/sentinel/di/component/DomainComponent.kt`
- Test: `sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationOverlayControllerTest.kt`

**Interfaces:**
- Consumes: `AnnotationCache`, `AnnotationOverlayView`, `CompositeTargetResolver`, `TargetSelection`, `Annotation`.
- Produces: `internal class AnnotationOverlayController(cache: AnnotationCache) : Application.ActivityLifecycleCallbacks` with `fun start(application: Application)`. `internal object AnnotationNoteDialog { fun show(context: Context, targetType: String, initial: String?, onSave: (String) -> Unit, onDelete: () -> Unit) }`.

- [ ] **Step 1: Write the failing instrumented test**

Create `AnnotationOverlayControllerTest.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.app.Activity
import android.view.ViewGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.data.sources.memory.annotations.InMemoryAnnotationCache
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AnnotationOverlayControllerTest {
    private fun overlayCount(activity: Activity): Int {
        val decor = activity.window.decorView as ViewGroup
        return (0 until decor.childCount).count { decor.getChildAt(it) is AnnotationOverlayView }
    }

    @Test
    fun `no overlay is attached while capture is off and there are no notes`() {
        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        controller.start(ApplicationProvider.getApplicationContext())

        ActivityScenario.launch(Activity::class.java).use { scenario ->
            scenario.onActivity { assertEquals(0, overlayCount(it)) }
        }
    }

    @Test
    fun `arming capture attaches the overlay to the resumed activity`() = runTest {
        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        controller.start(ApplicationProvider.getApplicationContext())

        ActivityScenario.launch(Activity::class.java).use { scenario ->
            cache.active.value = true
            scenario.onActivity { activity ->
                activity.window.decorView.post { assertEquals(1, overlayCount(activity)) }
            }
        }
    }

    @Test
    fun `disarming capture with no notes detaches the overlay`() = runTest {
        val cache = InMemoryAnnotationCache()
        val controller = AnnotationOverlayController(cache)
        controller.start(ApplicationProvider.getApplicationContext())

        ActivityScenario.launch(Activity::class.java).use { scenario ->
            cache.active.value = true
            cache.active.value = false
            scenario.onActivity { activity ->
                activity.window.decorView.post { assertEquals(0, overlayCount(activity)) }
            }
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :sentinel:connectedDebugAndroidTest --tests "*AnnotationOverlayControllerTest"`
Expected: FAIL — `Unresolved reference: AnnotationOverlayController`.

- [ ] **Step 3: Write the note dialog layout**

Create `sentinel/src/main/res/layout/sentinel_dialog_annotation_note.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:id="@+id/sentinel_titleView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:textAppearance="?attr/textAppearanceTitleMedium" />

    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/sentinel_noteInput"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="@string/sentinel_annotations_note_hint"
            android:inputType="textMultiLine"
            android:minLines="2"
            android:maxLines="5" />

    </com.google.android.material.textfield.TextInputLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:gravity="end"
        android:orientation="horizontal">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/sentinel_deleteButton"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/sentinel_delete"
            android:visibility="gone" />

        <android.widget.Space
            android:layout_width="0dp"
            android:layout_height="1dp"
            android:layout_weight="1" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/sentinel_cancelButton"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/sentinel_cancel" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/sentinel_saveButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:text="@string/sentinel_save" />

    </LinearLayout>

</LinearLayout>
```

- [ ] **Step 4: Write the note dialog**

Create `AnnotationNoteDialog.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelDialogAnnotationNoteBinding

/**
 * The note input sheet.
 *
 * Built against a [ContextThemeWrapper] carrying Sentinel's own theme rather than the host
 * Activity's: the host theme is arbitrary and may not supply the Material attributes
 * [BottomSheetDialog] needs.
 */
internal object AnnotationNoteDialog {
    fun show(
        context: Context,
        targetType: String,
        initial: String?,
        onSave: (String) -> Unit,
        onDelete: () -> Unit,
    ) {
        val themed = ContextThemeWrapper(context, R.style.Sentinel_Theme)
        val binding = SentinelDialogAnnotationNoteBinding.inflate(LayoutInflater.from(themed))
        val dialog = BottomSheetDialog(themed).apply { setContentView(binding.root) }

        val editing = initial != null
        binding.sentinelTitleView.text =
            themed.getString(
                if (editing) R.string.sentinel_annotations_note_edit else R.string.sentinel_annotations_note_new,
                targetType,
            )
        binding.sentinelNoteInput.setText(initial.orEmpty())
        binding.sentinelDeleteButton.visibility = if (editing) android.view.View.VISIBLE else android.view.View.GONE

        binding.sentinelDeleteButton.setOnClickListener {
            onDelete()
            dialog.dismiss()
        }
        binding.sentinelCancelButton.setOnClickListener { dialog.dismiss() }
        binding.sentinelSaveButton.setOnClickListener {
            val note = binding.sentinelNoteInput.text?.toString()?.trim().orEmpty()
            // An emptied note on an existing annotation means remove it, matching the sheet in borealis.
            if (note.isEmpty() && editing) onDelete() else if (note.isNotEmpty()) onSave(note)
            dialog.dismiss()
        }

        dialog.show()
    }
}
```

- [ ] **Step 5: Write the controller**

Create `AnnotationOverlayController.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.sources.memory.annotations.AnnotationCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Owns the capture overlay across the host app's activities.
 *
 * The overlay is attached to whichever Activity is resumed and is removed when neither capture
 * nor a saved note needs it, so an app with no annotations pays nothing. Marker positions are
 * recomputed by a pre-draw listener, which fires only on real draws and therefore tracks
 * scrolling, keyboard insets and rotation without special cases.
 */
internal class AnnotationOverlayController(
    private val cache: AnnotationCache,
) : Application.ActivityLifecycleCallbacks {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val resolver = CompositeTargetResolver()

    private var activity: Activity? = null
    private var overlay: AnnotationOverlayView? = null
    private var selected: ResolvedTarget? = null

    fun start(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        combine(cache.active, cache.load()) { active, annotations -> active to annotations }
            .onEach { (active, annotations) -> sync(active, annotations) }
            .launchIn(scope)
    }

    override fun onActivityResumed(activity: Activity) {
        this.activity = activity
        sync(cache.active.value, cache.load().value)
    }

    override fun onActivityPaused(activity: Activity) {
        if (this.activity === activity) {
            detach()
            this.activity = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    private fun sync(active: Boolean, annotations: List<Annotation>) {
        val host = activity ?: return
        if (!active && annotations.isEmpty()) {
            detach()
            return
        }
        val view = overlay ?: attach(host)
        view.captureActive = active
        if (!active) selected = null
        refreshMarkers(annotations)
    }

    private fun attach(host: Activity): AnnotationOverlayView {
        val view =
            AnnotationOverlayView(host).apply {
                onCaptureTap = { x, y -> handleCaptureTap(host, x, y) }
                onBadgeTapped = { annotation -> editNote(host, annotation) }
            }
        (host.window.decorView as ViewGroup).addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        host.window.decorView.viewTreeObserver.addOnPreDrawListener {
            refreshMarkers(cache.load().value)
            true
        }
        overlay = view
        return view
    }

    private fun detach() {
        val view = overlay ?: return
        (view.parent as? ViewGroup)?.removeView(view)
        overlay = null
        selected = null
    }

    /**
     * A tap on the already-selected element broadens the selection one step toward the screen
     * root, and wraps back to the deepest target past the outermost one. Anything else selects
     * afresh.
     *
     * Selecting never opens the note sheet: that would make broadening unreachable, because the
     * second tap would land on a dialog instead of the element. The sheet is opened from the
     * inspect card's "Add note" action, added in Task 10.
     */
    private fun handleCaptureTap(host: Activity, x: Int, y: Int) {
        val root = host.window.decorView
        val fresh = resolver.resolve(root, x, y) ?: return
        val current = selected

        val next =
            if (current != null && current.node.bounds.contains(x, y) && current.node.type == fresh.node.type) {
                TargetSelection.stepUp(current.node)?.let { ResolvedTarget(it, current.handle, current.toolkit) }
                    ?: fresh
            } else {
                fresh
            }

        selected = next
        overlay?.selection = next.node.bounds
    }

    /** Opens the note sheet for the current selection. Called by the inspect card in Task 10. */
    fun addNoteForSelection() {
        val host = activity ?: return
        val target = selected ?: return
        AnnotationNoteDialog.show(
            context = host,
            targetType = target.node.type,
            initial = null,
            onSave = { note -> saveNote(host, target, note) },
            onDelete = { },
        )
    }

    private fun saveNote(host: Activity, target: ResolvedTarget, note: String) {
        val root = host.window.decorView
        val tree =
            if (target.toolkit == com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit.COMPOSE) {
                ComposeSupport.rootNodeOf(root)
            } else {
                root.toTargetNode(parent = null)
            }
        val (ordinal, ordinalCount) =
            tree?.let { TargetSelection.ordinalOf(it, target.node) } ?: (1 to 1)

        scope.launch {
            val stored =
                cache.add(
                    Annotation(
                        id = "",
                        note = note,
                        toolkit = target.toolkit,
                        targetType = target.node.type,
                        targetPath = TargetSelection.pathOf(target.node),
                        viewId = target.node.viewId,
                        testTag = target.node.testTag,
                        sourceLocation = target.node.sourceLocation,
                        ordinal = ordinal,
                        ordinalCount = ordinalCount,
                        text = target.node.text,
                        screen = host.javaClass.simpleName,
                        fragment = fragmentNameOf(host, target.node.bounds),
                        route = null,
                        rebindX = target.node.bounds.centerX,
                        rebindY = target.node.bounds.centerY,
                    ),
                )
            stored.target = target.handle
            selected = null
            overlay?.selection = null
        }
    }

    private fun editNote(host: Activity, annotation: Annotation) {
        AnnotationNoteDialog.show(
            context = host,
            targetType = annotation.targetType,
            initial = annotation.note,
            onSave = { note -> scope.launch { cache.updateNote(annotation.id, note) } },
            onDelete = { scope.launch { cache.remove(annotation.id) } },
        )
    }

    /** Simple name of the deepest Fragment whose view covers [bounds]. */
    private fun fragmentNameOf(host: Activity, bounds: Bounds): String? {
        if (host !is FragmentActivity) return null
        var deepest: Fragment? = null
        fun visit(fragments: List<Fragment>) {
            fragments.forEach { fragment ->
                val view = fragment.view ?: return@forEach
                val location = IntArray(2)
                view.getLocationOnScreen(location)
                val frame = Bounds(location[0], location[1], location[0] + view.width, location[1] + view.height)
                if (frame.contains(bounds.centerX, bounds.centerY)) deepest = fragment
                visit(fragment.childFragmentManager.fragments)
            }
        }
        visit(host.supportFragmentManager.fragments)
        return deepest?.javaClass?.simpleName
    }

    /**
     * Recomputes marker positions from the live handles, and re-binds notes whose element was
     * recreated. Only bounds are read here — no tree walk — so this stays cheap per frame.
     */
    private fun refreshMarkers(annotations: List<Annotation>) {
        val host = activity ?: return
        val view = overlay ?: return
        val root = host.window.decorView

        val markers =
            annotations.mapIndexedNotNull { index, annotation ->
                if (annotation.screen != null && annotation.screen != host.javaClass.simpleName) return@mapIndexedNotNull null
                val bounds = boundsOf(annotation, root) ?: return@mapIndexedNotNull null
                AnnotationMarker(index + 1, bounds, annotation)
            }

        if (view.markers != markers) view.markers = markers
    }

    private fun boundsOf(annotation: Annotation, root: android.view.View): Bounds? {
        val handle = annotation.target
        if (handle is android.view.View && handle.isAttachedToWindow && handle.width > 0) {
            val location = IntArray(2)
            handle.getLocationOnScreen(location)
            return Bounds(location[0], location[1], location[0] + handle.width, location[1] + handle.height)
        }
        val rebound = resolver.rebind(annotation, root) ?: return null
        scope.launch { cache.rebind(annotation.id, rebound.handle) }
        return rebound.node.bounds
    }
}
```

- [ ] **Step 6: Run the test to verify it passes**

Run: `./gradlew :sentinel:connectedDebugAndroidTest --tests "*AnnotationOverlayControllerTest"`
Expected: PASS, 3 tests.

- [ ] **Step 7: Start the controller from DomainComponent**

In `sentinel/src/main/kotlin/com/infinum/sentinel/di/component/DomainComponent.kt`, add the imports:

```kotlin
import com.infinum.sentinel.data.sources.memory.annotations.AnnotationCache
import com.infinum.sentinel.ui.annotations.overlay.AnnotationOverlayController
```

Expose the cache and the controller, and start it from `setup()` next to the existing
`initializeBundleMonitor()` call:

```kotlin
    @Provides
    @DomainScope
    fun annotationOverlayController(cache: AnnotationCache): AnnotationOverlayController =
        AnnotationOverlayController(cache)

    private fun initializeAnnotations() {
        (this.context as? Application)?.let { annotationOverlayController.start(it) }
    }
```

Add `initializeAnnotations()` to the body of `setup()`. Match the surrounding style — read the
existing `setup()` before editing, since the exact scope annotation and accessor names must
line up with the other providers in that file.

- [ ] **Step 8: Verify and commit**

```bash
./gradlew :sentinel:compileDebugKotlin :sentinel:detekt
git add sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay \
        sentinel/src/main/res/layout/sentinel_dialog_annotation_note.xml \
        sentinel/src/main/kotlin/com/infinum/sentinel/di/component/DomainComponent.kt \
        sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay
git commit -m "feat: attach the annotation overlay across the host activities"
```

---

### Task 10: Capture chrome — inspect card and stop control

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationInspectCard.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationStopButton.kt`
- Create: `sentinel/src/main/res/layout/sentinel_view_annotation_inspect.xml`
- Create: `sentinel/src/main/res/drawable/sentinel_ic_close.xml`
- Modify: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationOverlayController.kt`
- Test: `sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay/AnnotationInspectCardTest.kt`

**Interfaces:**
- Consumes: `TargetNode`, `Bounds`, `AnnotationOverlayController.addNoteForSelection`.
- Produces: `internal class AnnotationInspectCard(context: Context)` with `fun showFor(node: TargetNode, viewport: Bounds)`, `fun hide()`, `var onAddNote: (() -> Unit)?`. `internal class AnnotationStopButton(context: Context)` with `var onStop: (() -> Unit)?`, `var onReopen: (() -> Unit)?`.

Task 9 selects an element but gives no way to act on it, and no way to leave capture mode. This
task supplies both. The card carries the "Add note" action, which is why selecting does not open
the sheet directly — a tap on the element must stay free to broaden the selection.

- [ ] **Step 1: Write the failing instrumented test**

Create `AnnotationInspectCardTest.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class AnnotationInspectCardTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    private class FakeNode(
        override val type: String,
        override val bounds: Bounds,
        override val text: String? = null,
        override val viewId: String? = null,
        override val sourceLocation: String? = null,
    ) : TargetNode {
        override val testTag: String? = null
        override val annotatable: Boolean = true
        override val children: List<TargetNode> = emptyList()
        override val parent: TargetNode? = null
    }

    @Test
    fun `the card shows the target type`() {
        val card = AnnotationInspectCard(context)

        card.showFor(FakeNode("MaterialButton", Bounds(0, 0, 100, 40)), Bounds(0, 0, 1080, 1920))

        assertEquals("MaterialButton", card.typeText())
        assertEquals(View.VISIBLE, card.visibility)
    }

    @Test
    fun `the card shows the resource id when one resolved`() {
        val card = AnnotationInspectCard(context)

        card.showFor(
            FakeNode("MaterialButton", Bounds(0, 0, 100, 40), viewId = "submitButton"),
            Bounds(0, 0, 1080, 1920),
        )

        assertTrue(card.detailText().contains("submitButton"))
    }

    @Test
    fun `the card prefers the source location over the resource id`() {
        val card = AnnotationInspectCard(context)

        card.showFor(
            FakeNode("PrimaryButton", Bounds(0, 0, 100, 40), viewId = "x", sourceLocation = "Checkout.kt:12"),
            Bounds(0, 0, 1080, 1920),
        )

        assertTrue(card.detailText().contains("Checkout.kt:12"))
    }

    @Test
    fun `hide removes the card from view`() {
        val card = AnnotationInspectCard(context)
        card.showFor(FakeNode("Button", Bounds(0, 0, 100, 40)), Bounds(0, 0, 1080, 1920))

        card.hide()

        assertEquals(View.GONE, card.visibility)
    }

    @Test
    fun `add note reports through the callback`() {
        var tapped = false
        val card = AnnotationInspectCard(context).apply { onAddNote = { tapped = true } }
        card.showFor(FakeNode("Button", Bounds(0, 0, 100, 40)), Bounds(0, 0, 1080, 1920))

        card.performAddNote()

        assertTrue(tapped)
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :sentinel:connectedDebugAndroidTest --tests "*AnnotationInspectCardTest"`
Expected: FAIL — `Unresolved reference: AnnotationInspectCard`.

- [ ] **Step 3: Add the close icon and the card layout**

Create `sentinel/src/main/res/drawable/sentinel_ic_close.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M19,6.41L17.59,5 12,10.59 6.41,5 5,6.41 10.59,12 5,17.59 6.41,19 12,13.41 17.59,19 19,17.59 13.41,12z" />
</vector>
```

Create `sentinel/src/main/res/layout/sentinel_view_annotation_inspect.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<merge xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto">

    <TextView
        android:id="@+id/typeView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:fontFamily="monospace"
        android:textAppearance="?attr/textAppearanceLabelLarge" />

    <TextView
        android:id="@+id/detailView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:fontFamily="monospace"
        android:textAppearance="?attr/textAppearanceLabelSmall" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/addNoteButton"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="@string/sentinel_annotations_add_note"
        app:icon="@drawable/sentinel_ic_annotations" />

</merge>
```

- [ ] **Step 4: Write the inspect card**

Create `AnnotationInspectCard.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import com.google.android.material.card.MaterialCardView
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelViewAnnotationInspectBinding

/**
 * The compact card that names the selected element and offers "Add note".
 *
 * Positioned just below the selection, flipped above it when there is no room, and clamped to
 * the viewport so it is never partly off screen. Built against Sentinel's own theme because the
 * host Activity's theme is arbitrary.
 */
@SuppressLint("ViewConstructor")
internal class AnnotationInspectCard(
    context: Context,
) : MaterialCardView(ContextThemeWrapper(context, R.style.Sentinel_Theme)) {
    private companion object {
        private const val MARGIN = 16
        private const val GAP = 16
        private const val MAX_WIDTH = 900
    }

    private val binding: SentinelViewAnnotationInspectBinding

    var onAddNote: (() -> Unit)? = null

    init {
        val content =
            LinearLayout(this.context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(MARGIN, MARGIN, MARGIN, MARGIN)
            }
        binding = SentinelViewAnnotationInspectBinding.inflate(LayoutInflater.from(this.context), content)
        addView(content)
        visibility = View.GONE
        binding.addNoteButton.setOnClickListener { onAddNote?.invoke() }
        TextViewCompat.setTextAppearance(binding.typeView, R.style.TextAppearance_AppCompat_Small)
    }

    /** Shows the card for [node], positioned against its bounds inside [viewport]. */
    fun showFor(node: TargetNode, viewport: Bounds) {
        binding.typeView.text = node.type
        // Views identify by resource id, composables by source location. Show whichever resolved,
        // then the element's text as a last resort.
        binding.detailView.text = node.sourceLocation ?: node.viewId ?: node.text.orEmpty()
        binding.detailView.visibility = if (binding.detailView.text.isNullOrEmpty()) View.GONE else View.VISIBLE
        visibility = View.VISIBLE

        measure(
            MeasureSpec.makeMeasureSpec(minOf(MAX_WIDTH, viewport.width - MARGIN * 2), MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(viewport.height, MeasureSpec.AT_MOST),
        )

        val below = node.bounds.bottom + GAP
        val above = node.bounds.top - GAP - measuredHeight
        val top =
            when {
                below + measuredHeight <= viewport.bottom - MARGIN -> below
                above >= viewport.top + MARGIN -> above
                else -> (viewport.bottom - measuredHeight - MARGIN).coerceAtLeast(viewport.top + MARGIN)
            }
        val maxLeft = (viewport.right - measuredWidth - MARGIN).coerceAtLeast(MARGIN)
        val left = node.bounds.left.coerceIn(viewport.left + MARGIN, maxLeft)

        updateLayoutParams<android.widget.FrameLayout.LayoutParams> {
            width = measuredWidth
            height = measuredHeight
            leftMargin = left - viewport.left
            topMargin = top - viewport.top
        }
    }

    fun hide() {
        visibility = View.GONE
    }

    /** Test seam: the rendered type text. */
    fun typeText(): String = binding.typeView.text.toString()

    /** Test seam: the rendered detail line. */
    fun detailText(): String = binding.detailView.text.toString()

    /** Test seam: triggers the add-note action without a synthetic touch. */
    fun performAddNote() {
        binding.addNoteButton.performClick()
    }
}
```

- [ ] **Step 5: Write the stop control**

Create `AnnotationStopButton.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.infinum.sentinel.R

/**
 * The draggable control that ends capture.
 *
 * Borealis turns its floating debug button into a close button while capture runs; Sentinel has
 * no floating button, so capture brings its own. A long press reopens the Sentinel panel with
 * capture still armed, matching that behaviour.
 */
@SuppressLint("ViewConstructor")
internal class AnnotationStopButton(
    context: Context,
) : FloatingActionButton(ContextThemeWrapper(context, R.style.Sentinel_Theme)) {
    private var downX = 0f
    private var downY = 0f
    private var dragging = false

    var onStop: (() -> Unit)? = null
    var onReopen: (() -> Unit)? = null

    init {
        setImageResource(R.drawable.sentinel_ic_close)
        contentDescription = context.getString(R.string.sentinel_annotations_stop)
        setOnClickListener { onStop?.invoke() }
        setOnLongClickListener {
            onReopen?.invoke()
            true
        }
    }

    /** Drags follow the finger; a tap still reaches the click listener. */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.rawX - translationX
                downY = event.rawY - translationY
                dragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - downX
                val dy = event.rawY - downY
                if (!dragging && (kotlin.math.abs(dx - translationX) > touchSlop ||
                        kotlin.math.abs(dy - translationY) > touchSlop)
                ) {
                    dragging = true
                }
                if (dragging) {
                    translationX = dx
                    translationY = dy
                    return true
                }
            }

            MotionEvent.ACTION_UP -> if (dragging) return true
        }
        return super.onTouchEvent(event)
    }

    private val touchSlop: Int
        get() = android.view.ViewConfiguration.get(context).scaledTouchSlop

    companion object {
        fun visibilityFor(captureActive: Boolean): Int = if (captureActive) View.VISIBLE else View.GONE
    }
}
```

- [ ] **Step 6: Wire both into the controller**

In `AnnotationOverlayController.kt`, hold the card and the button, add them to the decor view
next to the overlay, and drive them from the selection and the capture flag.

Add fields:

```kotlin
    private var card: AnnotationInspectCard? = null
    private var stopButton: AnnotationStopButton? = null
```

In `attach`, after adding the overlay:

```kotlin
        val decor = host.window.decorView as ViewGroup
        val inspectCard =
            AnnotationInspectCard(host).apply {
                onAddNote = {
                    hide()
                    addNoteForSelection()
                }
            }
        decor.addView(
            inspectCard,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ),
        )
        card = inspectCard

        val stop =
            AnnotationStopButton(host).apply {
                onStop = { cache.active.value = false }
                onReopen = { com.infinum.sentinel.Sentinel.show() }
            }
        decor.addView(
            stop,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                android.view.Gravity.BOTTOM or android.view.Gravity.END,
            ).apply { setMargins(0, 0, 48, 48) },
        )
        stopButton = stop
```

In `detach`, remove all three views and null the fields:

```kotlin
        listOfNotNull(overlay, card, stopButton).forEach { (it.parent as? ViewGroup)?.removeView(it) }
        overlay = null
        card = null
        stopButton = null
        selected = null
```

In `sync`, drive the stop button and clear the card when capture ends:

```kotlin
        stopButton?.visibility = AnnotationStopButton.visibilityFor(active)
        if (!active) card?.hide()
```

At the end of `handleCaptureTap`, show the card for the new selection:

```kotlin
        val decorBounds = boundsOfView(host.window.decorView)
        card?.showFor(next.node, decorBounds)
```

Add the helper:

```kotlin
    private fun boundsOfView(view: android.view.View): Bounds {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Bounds(location[0], location[1], location[0] + view.width, location[1] + view.height)
    }
```

In `addNoteForSelection`, clear the selection highlight once the sheet is dismissed by adding
`overlay?.selection = null` inside the existing `saveNote` coroutine — it is already there.

- [ ] **Step 7: Run the tests to verify they pass**

Run: `./gradlew :sentinel:connectedDebugAndroidTest --tests "*AnnotationInspectCardTest" --tests "*AnnotationOverlayControllerTest"`
Expected: PASS, 8 tests total.

- [ ] **Step 8: Commit**

```bash
./gradlew :sentinel:detekt
git add sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/overlay         sentinel/src/main/res         sentinel/src/androidTest/kotlin/com/infinum/sentinel/ui/annotations/overlay
git commit -m "feat: add the inspect card and the capture stop control"
```


---

### Task 11: Annotations screen

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/AnnotationsActivity.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/AnnotationsFragment.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/AnnotationsViewModel.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/AnnotationsState.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/AnnotationsEvent.kt`
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations/AnnotationsAdapter.kt`
- Create: `sentinel/src/main/res/layout/sentinel_fragment_annotations.xml`
- Create: `sentinel/src/main/res/layout/sentinel_item_annotation.xml`
- Create: `sentinel/src/main/res/menu/sentinel_annotations_menu.xml`
- Create: `sentinel/src/main/res/drawable/sentinel_ic_annotations.xml`
- Modify: `sentinel/src/main/AndroidManifest.xml`
- Modify: `sentinel/src/main/kotlin/com/infinum/sentinel/di/component/ViewModelComponent.kt`

**Interfaces:**
- Consumes: `AnnotationCache`, `AnnotationExporter`, `Annotation`.
- Produces: `internal class AnnotationsActivity : BaseChildActivity`; `internal class AnnotationsViewModel(cache, exporter) : BaseChildViewModel<AnnotationsState, AnnotationsEvent>` with `fun arm()`, `fun delete(id: String)`, `fun clear()`, `fun export(context: Context)`.

- [ ] **Step 1: Write the state and event types**

Create `AnnotationsState.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation

internal sealed class AnnotationsState {
    data class Data(
        val annotations: List<Annotation>,
    ) : AnnotationsState()
}
```

Create `AnnotationsEvent.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations

internal sealed class AnnotationsEvent {
    /** Capture armed: the screen closes so the user lands back on the live app. */
    data object CaptureArmed : AnnotationsEvent()

    data class ExportFinished(
        val message: String,
    ) : AnnotationsEvent()

    data class ExportFailed(
        val message: String,
    ) : AnnotationsEvent()
}
```

- [ ] **Step 2: Write the view model**

Create `AnnotationsViewModel.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.infinum.sentinel.R
import com.infinum.sentinel.data.sources.memory.annotations.AnnotationCache
import com.infinum.sentinel.domain.annotations.AnnotationExporter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import me.tatarka.inject.annotations.Inject

@Inject
internal class AnnotationsViewModel(
    private val cache: AnnotationCache,
    private val exporter: AnnotationExporter,
) : BaseChildViewModel<AnnotationsState, AnnotationsEvent>() {
    override fun data() {
        cache
            .load()
            .onEach { setState(AnnotationsState.Data(it)) }
            .launchIn(viewModelScope)
    }

    fun exporterIcon(): Int = exporter.icon()

    fun exporterLabel(): Int = exporter.label()

    /** Arms capture and asks the screen to close so the live app is visible. */
    fun arm() =
        launch {
            cache.active.value = true
            emitEvent(AnnotationsEvent.CaptureArmed)
        }

    fun delete(id: String) = launch { cache.remove(id) }

    fun clear() = launch { cache.clear() }

    /**
     * Exports the notes that have not been sent yet, then marks them sent. Reports the
     * exporter's own status message, or the failure it threw.
     */
    fun export(context: Context) =
        launch {
            val pending = cache.load().value.filterNot { it.isSent }
            if (pending.isEmpty()) {
                emitEvent(AnnotationsEvent.ExportFailed(context.getString(R.string.sentinel_annotations_empty_export)))
                return@launch
            }
            try {
                val message = exporter.export(context, pending)
                cache.markSent()
                emitEvent(AnnotationsEvent.ExportFinished(message))
            } catch (error: Exception) {
                emitEvent(
                    AnnotationsEvent.ExportFailed(
                        context.getString(R.string.sentinel_annotations_export_failed, error.message.orEmpty()),
                    ),
                )
            }
        }
}
```

`catch (error: Exception)` is deliberate: an exporter is third-party code and any failure must
land in a snackbar rather than crash the debug tool. Detekt's `TooGenericExceptionCaught` may
flag it — add `@Suppress("TooGenericExceptionCaught")` on `export` if it does.

- [ ] **Step 3: Write the layouts**

Create `sentinel/src/main/res/layout/sentinel_fragment_annotations.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@color/sentinel_surface"
        app:menu="@menu/sentinel_annotations_menu"
        app:title="@string/sentinel_annotations"
        app:layout_constraintTop_toTopOf="parent" />

    <LinearLayout
        android:id="@+id/actionsLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        app:layout_constraintTop_toBottomOf="@id/toolbar">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/startButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="@string/sentinel_annotations_start"
            app:icon="@drawable/sentinel_ic_annotations" />

        <com.google.android.material.button.MaterialButton
            android:id="@+id/exportButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_marginStart="12dp"
            android:layout_weight="1" />

    </LinearLayout>

    <LinearLayout
        android:id="@+id/emptyLayout"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:gravity="center"
        android:orientation="vertical"
        android:visibility="gone"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintTop_toBottomOf="@id/toolbar">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/enableButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/sentinel_annotations_enable"
            app:icon="@drawable/sentinel_ic_annotations" />

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="12dp"
            android:paddingStart="32dp"
            android:paddingEnd="32dp"
            android:gravity="center"
            android:text="@string/sentinel_annotations_hint"
            android:textAppearance="?attr/textAppearanceBodySmall" />

    </LinearLayout>

    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintTop_toBottomOf="@id/actionsLayout"
        tools:listitem="@layout/sentinel_item_annotation"
        xmlns:tools="http://schemas.android.com/tools" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

Create `sentinel/src/main/res/layout/sentinel_item_annotation.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:paddingStart="16dp"
    android:paddingTop="12dp"
    android:paddingEnd="16dp"
    android:paddingBottom="12dp">

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">

        <TextView
            android:id="@+id/noteView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textAppearance="?attr/textAppearanceBodyLarge" />

        <TextView
            android:id="@+id/targetView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="6dp"
            android:textAppearance="?attr/textAppearanceLabelSmall" />

        <TextView
            android:id="@+id/locationView"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:textAppearance="?attr/textAppearanceLabelSmall" />

    </LinearLayout>

    <TextView
        android:id="@+id/sentView"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginEnd="12dp"
        android:text="@string/sentinel_annotations_sent_label"
        android:textAppearance="?attr/textAppearanceLabelLarge"
        android:visibility="gone" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/deleteButton"
        style="@style/Widget.Material3.Button.IconButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:icon="@drawable/sentinel_ic_clear"
        xmlns:app="http://schemas.android.com/apk/res-auto" />

</LinearLayout>
```

Create `sentinel/src/main/res/menu/sentinel_annotations_menu.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    tools:ignore="AlwaysShowAction">

    <item
        android:id="@+id/clear"
        android:icon="@drawable/sentinel_ic_clear"
        android:title="@string/sentinel_clear"
        app:showAsAction="always" />

</menu>
```

Create `sentinel/src/main/res/drawable/sentinel_ic_annotations.xml`:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M3,10h11v2H3zM3,6h11v2H3zM3,14h7v2H3zM16.4,15.19l3.54,-3.54 1.41,1.41 -3.54,3.54zM17.5,20H16v-1.5l4.6,-4.6 1.5,1.5z" />
</vector>
```

- [ ] **Step 4: Write the adapter**

Create `AnnotationsAdapter.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.databinding.SentinelItemAnnotationBinding

internal class AnnotationsAdapter(
    private val onDelete: (Annotation) -> Unit,
) : ListAdapter<Annotation, AnnotationsAdapter.ViewHolder>(DIFF) {
    private companion object {
        private const val SENT_ALPHA = 0.6f
        private const val ACTIVE_ALPHA = 1.0f

        private val DIFF =
            object : DiffUtil.ItemCallback<Annotation>() {
                override fun areItemsTheSame(oldItem: Annotation, newItem: Annotation): Boolean = oldItem.id == newItem.id

                override fun areContentsTheSame(oldItem: Annotation, newItem: Annotation): Boolean = oldItem == newItem
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            SentinelItemAnnotationBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onDelete,
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), position)

    internal class ViewHolder(
        private val binding: SentinelItemAnnotationBinding,
        private val onDelete: (Annotation) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(annotation: Annotation, position: Int) {
            with(binding) {
                root.alpha = if (annotation.isSent) SENT_ALPHA else ACTIVE_ALPHA
                noteView.text = root.context.getString(R.string.sentinel_annotations_row, position + 1, annotation.note)
                targetView.text = annotation.targetType
                // Views identify by resource id, composables by source location. Show whichever resolved.
                val locator = annotation.sourceLocation ?: annotation.viewId
                locationView.text = locator
                locationView.visibility = if (locator == null) View.GONE else View.VISIBLE
                sentView.visibility = if (annotation.isSent) View.VISIBLE else View.GONE
                deleteButton.setOnClickListener { confirmDelete(annotation, position) }
            }
        }

        /** Confirms first, so a stray tap cannot silently drop a note. */
        private fun confirmDelete(annotation: Annotation, position: Int) {
            MaterialAlertDialogBuilder(binding.root.context)
                .setTitle(R.string.sentinel_annotations_delete_title)
                .setMessage(binding.root.context.getString(R.string.sentinel_annotations_delete_message, position + 1))
                .setNegativeButton(R.string.sentinel_cancel, null)
                .setPositiveButton(R.string.sentinel_delete) { _, _ -> onDelete(annotation) }
                .show()
        }
    }
}
```

Add the row format string to `sentinel/src/main/res/values/strings.xml`:

```xml
    <string name="sentinel_annotations_row">#%1$d %2$s</string>
```

- [ ] **Step 5: Write the fragment and activity**

Create `AnnotationsFragment.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.google.android.material.snackbar.Snackbar
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentAnnotationsBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class AnnotationsFragment : BaseChildFragment<AnnotationsState, AnnotationsEvent>(
    R.layout.sentinel_fragment_annotations,
) {
    companion object {
        fun newInstance() = AnnotationsFragment()

        const val TAG: String = "AnnotationsFragment"
    }

    override val viewModel: AnnotationsViewModel by viewModels()

    override val binding: SentinelFragmentAnnotationsBinding by viewBinding(
        SentinelFragmentAnnotationsBinding::bind,
    )

    private val adapter = AnnotationsAdapter(onDelete = { viewModel.delete(it.id) })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            recyclerView.adapter = adapter
            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.clear) viewModel.clear()
                true
            }
            startButton.setOnClickListener { viewModel.arm() }
            enableButton.setOnClickListener { viewModel.arm() }
            exportButton.setIconResource(viewModel.exporterIcon())
            exportButton.setText(viewModel.exporterLabel())
            exportButton.setOnClickListener { viewModel.export(requireContext().applicationContext) }
        }
    }

    override fun onState(state: AnnotationsState) =
        when (state) {
            is AnnotationsState.Data -> {
                adapter.submitList(state.annotations)
                val empty = state.annotations.isEmpty()
                binding.emptyLayout.visibility = if (empty) View.VISIBLE else View.GONE
                binding.actionsLayout.visibility = if (empty) View.GONE else View.VISIBLE
                binding.recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
            }
        }

    override fun onEvent(event: AnnotationsEvent) =
        when (event) {
            // Close so the user lands on the live app with capture armed.
            is AnnotationsEvent.CaptureArmed -> requireActivity().finish()
            is AnnotationsEvent.ExportFinished -> showMessage(event.message)
            is AnnotationsEvent.ExportFailed -> showMessage(event.message)
        }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
```

Create `AnnotationsActivity.kt`:

```kotlin
package com.infinum.sentinel.ui.annotations

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class AnnotationsActivity : BaseChildActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, AnnotationsFragment.newInstance(), AnnotationsFragment.TAG)
            .commit()
    }
}
```

- [ ] **Step 6: Register the activity and the view model**

In `sentinel/src/main/AndroidManifest.xml`, alongside the other activities:

```xml
        <activity
            android:name="com.infinum.sentinel.ui.annotations.AnnotationsActivity"
            android:exported="false"
            android:label="@string/sentinel_name"
            android:taskAffinity="com.infinum.sentinel"
            android:theme="@style/Sentinel.Theme.Leaf">
            <meta-data
                android:name="@string/sentinel_infinum_monitored"
                android:value="false"/>
        </activity>
```

In `di/component/ViewModelComponent.kt` add the import, the abstract accessor and the map entry,
copying the shape of the existing `bundles` entries exactly:

```kotlin
import com.infinum.sentinel.ui.annotations.AnnotationsViewModel

    abstract val annotations: AnnotationsViewModel

    @IntoMap
    @Provides
    fun annotations(): Pair<Class<*>, ViewModel> = AnnotationsViewModel::class.java to annotations
```

`AnnotationsViewModel` needs an `AnnotationExporter` binding, which Task 12 provides. Until then
the KSP step fails with a missing binding — that is expected and is resolved by the next task.

- [ ] **Step 7: Commit**

The build does not compile on its own until Task 12 supplies the exporter binding, so commit
without a green build here and verify at the end of Task 12.

```bash
git add sentinel/src/main/kotlin/com/infinum/sentinel/ui/annotations \
        sentinel/src/main/res sentinel/src/main/AndroidManifest.xml \
        sentinel/src/main/kotlin/com/infinum/sentinel/di/component/ViewModelComponent.kt
git commit -m "feat: add the annotations screen"
```

---

### Task 12: The tool, the exporter binding and default registration

**Files:**
- Create: `sentinel/src/main/kotlin/com/infinum/sentinel/ui/tools/AnnotationsTool.kt`
- Modify: `sentinel/src/main/kotlin/com/infinum/sentinel/di/component/DataComponent.kt`
- Modify: `sentinel/src/main/kotlin/com/infinum/sentinel/di/LibraryComponents.kt`
- Create: `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/ui/tools/AnnotationsTool.kt`
- Test: `sentinel/src/test/kotlin/com/infinum/sentinel/di/DefaultToolsTest.kt`

**Interfaces:**
- Consumes: `AnnotationExporter`, `ClipboardAnnotationExporter`, `AnnotationsActivity`.
- Produces: `public class AnnotationsTool(exporter: AnnotationExporter = ClipboardAnnotationExporter()) : Sentinel.Tool` exposing `public val exporter: AnnotationExporter`. `internal fun resolveTools(userTools: Set<Sentinel.Tool>, defaults: Set<Sentinel.Tool>): Set<Sentinel.Tool>`.

- [ ] **Step 1: Write the failing test for default-tool resolution**

Create `sentinel/src/test/kotlin/com/infinum/sentinel/di/DefaultToolsTest.kt`:

```kotlin
package com.infinum.sentinel.di

import android.view.View
import com.infinum.sentinel.Sentinel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class DefaultToolsTest {
    private class FakeTool(private val id: Int) : Sentinel.Tool {
        override fun name(): Int = id

        override fun listener(): View.OnClickListener = View.OnClickListener { }
    }

    private class OtherFakeTool : Sentinel.Tool {
        override fun name(): Int = 99

        override fun listener(): View.OnClickListener = View.OnClickListener { }
    }

    @Test
    fun `defaults are added when the user supplies none`() {
        val defaults = setOf<Sentinel.Tool>(FakeTool(1))

        val actual = resolveTools(userTools = emptySet(), defaults = defaults)

        assertEquals(defaults, actual)
    }

    @Test
    fun `a user tool of the same type replaces the default`() {
        val userTool = FakeTool(2)

        val actual = resolveTools(userTools = setOf(userTool), defaults = setOf(FakeTool(1)))

        assertEquals(1, actual.size)
        assertTrue(actual.first() === userTool)
    }

    @Test
    fun `a user tool of a different type is added alongside the defaults`() {
        val userTool = OtherFakeTool()

        val actual = resolveTools(userTools = setOf(userTool), defaults = setOf(FakeTool(1)))

        assertEquals(2, actual.size)
        assertTrue(actual.any { it === userTool })
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*DefaultToolsTest"`
Expected: FAIL — `Unresolved reference: resolveTools`.

- [ ] **Step 3: Write the resolution helper**

In `sentinel/src/main/kotlin/com/infinum/sentinel/di/LibraryComponents.kt`, add a top-level
internal function above the `object`:

```kotlin
/**
 * The tools Sentinel shows: the defaults, with any default replaced by a user-supplied tool of
 * the same type. This lets a configurable tool such as [com.infinum.sentinel.ui.tools.AnnotationsTool]
 * ship enabled by default while still accepting configuration through [Sentinel.watch].
 */
internal fun resolveTools(
    userTools: Set<Sentinel.Tool>,
    defaults: Set<Sentinel.Tool>,
): Set<Sentinel.Tool> {
    val overriddenTypes = userTools.map { it::class }.toSet()
    return userTools + defaults.filterNot { it::class in overriddenTypes }
}
```

- [ ] **Step 4: Run the test to verify it passes**

Run: `./gradlew :sentinel:testDebugUnitTest --tests "*DefaultToolsTest"`
Expected: PASS, 3 tests.

- [ ] **Step 5: Write the tool**

Create `sentinel/src/main/kotlin/com/infinum/sentinel/ui/tools/AnnotationsTool.kt`:

```kotlin
package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.annotations.AnnotationExporter
import com.infinum.sentinel.domain.annotations.ClipboardAnnotationExporter
import com.infinum.sentinel.ui.annotations.AnnotationsActivity

/**
 * Attaches free-text notes to elements on the running app and exports them.
 *
 * Ships enabled with [ClipboardAnnotationExporter]. Pass an instance to [Sentinel.watch] to
 * change the destination, for example
 * `AnnotationsTool(ClaudeRoutineExporter({ BuildConfig.CLAUDE_CODE_TOKEN }, fireUrl))`.
 */
public class AnnotationsTool(
    public val exporter: AnnotationExporter = ClipboardAnnotationExporter(),
) : Sentinel.Tool {
    @StringRes
    override fun name(): Int = R.string.sentinel_annotations

    override fun icon(): Int = R.drawable.sentinel_ic_annotations

    override fun listener(): View.OnClickListener =
        View.OnClickListener {
            it.context.startActivity(
                Intent(it.context, AnnotationsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
            )
        }
}
```

- [ ] **Step 6: Register the default and provide the exporter**

In `LibraryComponents.kt`, add `AnnotationsTool()` to `DEFAULT_TOOLS`:

```kotlin
    private val DEFAULT_TOOLS =
        setOf(
            CrashMonitorTool(),
            BundleMonitorTool(),
            AppInfoTool(),
            AnnotationsTool(),
        )
```

and replace the `tools.plus(DEFAULT_TOOLS)` argument in `setup` with `resolveTools(tools, DEFAULT_TOOLS)`.

In `DataComponent.kt`, expose the exporter chosen by whichever `AnnotationsTool` ended up in the
set, mirroring how `CertificateTool` is already special-cased in `LibraryComponents.setup`:

```kotlin
import com.infinum.sentinel.domain.annotations.AnnotationExporter
import com.infinum.sentinel.domain.annotations.ClipboardAnnotationExporter
import com.infinum.sentinel.ui.tools.AnnotationsTool

    @Provides
    @DataScope
    fun annotationExporter(): AnnotationExporter =
        tools.filterIsInstance<AnnotationsTool>().firstOrNull()?.exporter ?: ClipboardAnnotationExporter()
```

`tools` is the constructor-supplied `Set<Sentinel.Tool>` already present in `DataComponent`.
Read the file first to confirm the property name before editing.

- [ ] **Step 7: Add the no-op stub**

Create `sentinel-no-op/src/main/kotlin/com/infinum/sentinel/ui/tools/AnnotationsTool.kt`:

```kotlin
package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.annotations.AnnotationExporter
import com.infinum.sentinel.domain.annotations.ClipboardAnnotationExporter

@Suppress("unused")
public class AnnotationsTool(
    public val exporter: AnnotationExporter = ClipboardAnnotationExporter(),
) : Sentinel.Tool {
    override fun name(): Int = 0

    override fun icon(): Int = 0

    override fun listener(): View.OnClickListener = View.OnClickListener { }
}
```

- [ ] **Step 8: Verify the whole module builds and all tests pass**

Run:

```bash
./gradlew :sentinel:assembleDebug :sentinel-no-op:assembleDebug
./gradlew :sentinel:testDebugUnitTest
./gradlew :sentinel:apiDump :sentinel-no-op:apiDump
./gradlew compareSentinelApis :sentinel:detekt :sentinel:lintDebug
```

Expected: all green. This is the first point where the KSP graph is complete, so a missing
binding surfaces here.

- [ ] **Step 9: Commit**

```bash
git add sentinel/src/main/kotlin/com/infinum/sentinel/ui/tools/AnnotationsTool.kt \
        sentinel/src/main/kotlin/com/infinum/sentinel/di \
        sentinel/src/test/kotlin/com/infinum/sentinel/di \
        sentinel-no-op/src/main/kotlin/com/infinum/sentinel/ui/tools/AnnotationsTool.kt \
        sentinel/api/sentinel.api sentinel-no-op/api/sentinel-no-op.api
git commit -m "feat: ship the annotations tool with a pluggable exporter"
```

---

### Task 13: Exercise the Compose path in the sample app

**Files:**
- Modify: `sample/build.gradle.kts`
- Modify: `gradle/libs.versions.toml`
- Create: `sample/src/main/kotlin/com/infinum/sentinel/sample/ComposeSampleActivity.kt`
- Modify: `sample/src/main/AndroidManifest.xml`
- Modify: `sample/src/main/kotlin/com/infinum/sentinel/sample/SampleApplication.kt`

Without this the Compose resolver ships untested against a real composition. This is the task
that proves `Group.location` actually yields `file:line` on device.

- [ ] **Step 1: Add Compose to the sample**

In `gradle/libs.versions.toml` under `[libraries]`, add the runtime artifacts the sample needs
(the `compose` version reference already exists from Task 7):

```toml
compose-material3 = { module = "androidx.compose.material3:material3", version = "1.4.0" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling", version.ref = "compose" }
compose-activity = { module = "androidx.activity:activity-compose", version = "1.11.0" }
```

Under `[plugins]`:

```toml
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

In `sample/build.gradle.kts`, add the plugin, enable the build feature and add the dependencies:

```kotlin
plugins {
    alias(libs.plugins.compose.compiler)
}

android {
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    // ui-tooling is what puts ui-tooling-data on the runtime classpath, which is what lets
    // Sentinel read composable source locations. Without it annotations still work, but with
    // no file:line.
    debugImplementation(libs.compose.ui.tooling)
}
```

Read `sample/build.gradle.kts` first and merge these into the existing blocks rather than
appending duplicates. Verify the `kotlin` version reference name matches the catalog.

- [ ] **Step 2: Write a Compose screen**

Create `sample/src/main/kotlin/com/infinum/sentinel/sample/ComposeSampleActivity.kt`:

```kotlin
package com.infinum.sentinel.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

class ComposeSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CheckoutScreen()
            }
        }
    }
}

@Composable
private fun CheckoutScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Checkout")
        PrimaryButton(label = "Submit")
        PrimaryButton(label = "Cancel")
    }
}

@Composable
private fun PrimaryButton(label: String) {
    Button(
        onClick = { },
        modifier = Modifier.testTag("primary_button"),
    ) {
        Text(text = label)
    }
}
```

- [ ] **Step 3: Register the activity and add a way to reach it**

In `sample/src/main/AndroidManifest.xml`, inside `<application>`:

```xml
        <activity
            android:name=".ComposeSampleActivity"
            android:exported="false" />
```

Add a button to `MainActivity` that starts it. Read `MainActivity.kt` and its layout, and follow
whatever pattern the existing buttons use.

- [ ] **Step 4: Configure the sample to send to Claude**

In `SampleApplication.kt`, pass a configured tool to `Sentinel.watch`. Read the file first and
merge into the existing tool set:

```kotlin
AnnotationsTool(
    exporter =
        ClaudeRoutineExporter(
            token = { BuildConfig.CLAUDE_CODE_TOKEN },
            fireUrl = BuildConfig.CLAUDE_ROUTINE_FIRE_URL,
        ),
),
```

Back those with `local.properties` so no credential is committed. In `sample/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        val localProperties =
            java.util.Properties().apply {
                val file = rootProject.file("local.properties")
                if (file.exists()) file.inputStream().use { load(it) }
            }
        buildConfigField(
            "String",
            "CLAUDE_CODE_TOKEN",
            "\"${localProperties.getProperty("claude.code.token", "")}\"",
        )
        buildConfigField(
            "String",
            "CLAUDE_ROUTINE_FIRE_URL",
            "\"${localProperties.getProperty("claude.routine.fireUrl", "")}\"",
        )
    }
    buildFeatures {
        buildConfig = true
    }
}
```

Confirm `local.properties` is in `.gitignore` before adding any value to it. It is by default in
Android projects; verify rather than assume.

- [ ] **Step 5: Verify on a device**

Run: `./gradlew :sample:installDebug`

Then by hand: open the sample, trigger Sentinel, open Tools, tap **Annotations**, tap **Enable
annotations**, navigate to the Compose screen, tap the **Submit** button, save a note. Reopen
Sentinel and confirm the row shows `ComposeSampleActivity.kt:<line>` as its locator, and that
the note's badge is drawn over the button and follows it.

Expected: a `sourceLocation` is present. If it is null, confirm `ui-tooling` is on the debug
runtime classpath — that is the usual cause.

- [ ] **Step 6: Commit**

```bash
git add sample gradle/libs.versions.toml
git commit -m "feat: exercise the compose annotation path in the sample app"
```

---

### Task 14: Documentation

**Files:**
- Modify: `README.md`
- Modify: `CHANGELOG.md`
- Create: `claude-annotations-instructions.md`

- [ ] **Step 1: Add the tool to the README's built-in list**

In `README.md`, in the `#### Built in` list, after the `CertificateTool` entry:

```markdown
- `AnnotationsTool` - attach free-text notes to elements on the running app and export them to
  the clipboard or to a Claude Code routine
```

- [ ] **Step 2: Add the Annotations section**

Add a `### Annotations` section after the `### Formatters` section:

```markdown
### Annotations

Attach free-text notes to elements on the running app, then export them so a coding agent can
apply the changes.

- Open **Tools** and tap **Annotations**, then **Enable annotations**. Sentinel closes and the
  live app returns with capture armed. Tap any element to select it and write a note.
- Tapping the already-selected element widens the selection to its parent, so a note can target a
  whole card rather than the label inside it.
- Saved notes stay on screen as a numbered badge and an outline. They follow the element as the
  screen scrolls and survive navigating away and back. Tap a badge to edit or delete that note.
- The export button copies every unsent note as JSON, or sends it to a Claude Code routine.

Each note records the element's type, its `R.id` resource entry name, its position among
same-type siblings, the Activity and Fragment it belongs to, and — for Compose — the composable
name, its `Modifier.testTag` and its `file:line`.

#### Compose support

Compose targets are resolved through the Compose tooling data API. Source locations require
`androidx.compose.ui:ui-tooling` on the debug runtime classpath:

```kotlin
debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
```

Without it, composables are still annotatable but report no `file:line`. Reading a live
composition uses reflection against non-public Compose internals, so an unsupported Compose
version degrades to annotating the hosting `ComposeView` instead of crashing.

#### Sending to Claude

```kotlin
Sentinel.watch(
    setOf(
        AnnotationsTool(
            exporter = ClaudeRoutineExporter(
                token = { BuildConfig.CLAUDE_CODE_TOKEN },
                fireUrl = "https://api.anthropic.com/v1/claude_code/routines/<trigger-id>/fire",
            ),
        ),
    ),
)
```

`token` is a lambda so the credential can come from `local.properties` through `BuildConfig`
rather than a string literal. **Treat this token as a secret.** It is a long-lived Claude Code
OAuth credential with your full permissions, and anyone holding the APK can extract a value
compiled into it. Never commit one.

`claude-annotations-instructions.md` in this repository is the routine prompt: it parses the
exported batch, groups the notes by screen, applies each change and opens a pull request.

#### Permissions

Sending to Claude needs network access, so the `sentinel` module declares:

```xml
<uses-permission android:name="android.permission.INTERNET"/>
```

This merges into your app's debug manifest. `sentinel-no-op`, used for release builds, declares
nothing.
```

- [ ] **Step 3: Write the routine prompt**

Create `claude-annotations-instructions.md` at the repository root. Port the borealis version at
`../flutter-borealis/claude-annotations-instructions.md`, changing:

- the field table to `id`, `note`, `toolkit`, `targetType`, `targetPath`, `viewId`, `testTag`,
  `sourceLocation`, `ordinal`/`ordinalCount`, `text`, `screen`, `fragment`, `route`
- the location guidance to: check `sourceLocation` first for Compose; for Views search for
  `R.id.<viewId>` and `@+id/<viewId>`; fall back to `screen` plus `fragment` plus `targetType`
  narrowed by `text` or the ordinal position
- grouping to `screen` first, then `fragment`
- the subagent's verification step from `flutter analyze` to
  `./gradlew detekt lintDebug` on the changed modules

Keep the rest — the grouping rules, the parallel-subagent dispatch, the PR body format and the
watch-the-comments workflow — as it is.

- [ ] **Step 4: Add the changelog entry**

Read `CHANGELOG.md` and add an entry in the existing format:

```markdown
### Added
- `AnnotationsTool`: attach notes to elements on the running app and export them to the clipboard
  or to a Claude Code routine. Supports both Views and Compose.
```

- [ ] **Step 5: Verify the docs build**

Run: `./gradlew :sentinel:generateReadme`
Expected: BUILD SUCCESSFUL. The task substitutes version strings; confirm the new section
survived unchanged.

- [ ] **Step 6: Commit**

```bash
git add README.md CHANGELOG.md claude-annotations-instructions.md
git commit -m "docs: document the annotations tool"
```

---

## Final verification

- [ ] Run the full gate: `./gradlew :sentinel:assembleDebug :sentinel-no-op:assembleDebug :sentinel:testDebugUnitTest :sentinel:detekt :sentinel:lintDebug compareSentinelApis`
- [ ] Run the instrumented suite with a device attached: `./gradlew :sentinel:connectedDebugAndroidTest`
- [ ] Confirm `git status` is clean and no commit was pushed.
