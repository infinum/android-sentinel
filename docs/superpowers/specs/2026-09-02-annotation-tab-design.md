# Annotation capture and Claude routine export for Sentinel

Date: 2026-09-02
Status: Approved design, not yet implemented

## Purpose

Port the annotation feature from the Flutter library `borealis`
(`lib/src/annotation/`, ~2300 lines) to native Android in `android-sentinel`.

A developer or QA tester taps a widget in the running app, attaches a free-text
note to it, and sends the collected notes to a Claude Code routine. The routine
locates each annotated element in the source, applies the requested change, and
opens a pull request.

Sentinel captures identity for each annotated element that is precise enough for
an agent to find the element in source without a screenshot: element type, the
resource id or test tag, source location where available, position among
same-type siblings, and the screen it belongs to.

## Source material

The Flutter implementation this port follows:

| Borealis file | Responsibility |
|---|---|
| `annotation_store.dart` | In-memory note list, `active` flag, ids, `isSent` |
| `annotation_model.dart` | The `Annotation` value type and its export JSON |
| `annotation_exporter.dart` | `AnnotationExporter` strategy, clipboard and Claude implementations |
| `ui/annotation_overlay.dart` | Full-screen capture layer, capture and passive modes |
| `ui/annotation_resolver.dart` | Hit test, nearest annotatable, step up, ordinal, rebind |
| `ui/annotation_target.dart` | Type, key, source location, display properties |
| `ui/annotation_markers.dart`, `ui/annotation_marker_tracker.dart` | Numbered badges and outlines that follow their widget |
| `ui/annotation_note_sheet.dart` | Note input sheet |
| `annotation_borealis_page.dart` | The tab: list, delete, clear, export |
| `claude-annotations-instructions.md` | The routine prompt |

## Decisions

These were settled before this document was written.

1. The feature is reached from the **Tools page**, not a new bottom navigation
   destination. `BottomNavigationView` caps at five items and all five slots in
   `sentinel_bottom_navigation.xml` are taken (`device`, `application`, `blank`,
   `permissions`, `preferences`), where `blank` is a disabled spacer for the
   centered FAB cradle. Packaging the feature as a `Sentinel.Tool` matches how
   `CrashMonitorTool` and `BundleMonitorTool` already work and needs no layout
   change.
2. **Both Views and Compose** are supported as annotation targets.
3. Everything lives in the **`sentinel` core module**. Compose support is a
   `compileOnly` dependency guarded by `Class.forName`, so apps that do not use
   Compose pull nothing extra.

## Feasibility, verified

The riskiest claim was that a Compose composable's `file:line` can be recovered
in process. It can. Square's Radiography does exactly this in
`radiography/src/main/java/radiography/internal/ComposeViews.kt`:

```
View.mKeyedTags (reflection) -> Composition
  -> WrappedComposition.original -> CompositionImpl.composer (reflection)
  -> composer.compositionData.asTree()          // ui-tooling-data, @UiToolingDataApi
  -> Group.location -> SourceLocation(sourceFile, lineNumber)
(composeView as RootForTest).semanticsOwner     // testTag, text, role, bounds
```

Constraints that come with it:

- The reflection into `CompositionImpl` is not public API and breaks across
  Compose versions. Radiography catches `LinkageError` and degrades with an
  "unsupported Compose version" message. This port does the same.
- `Group.location` requires the Compose compiler's source information, which is
  present in debug builds.
- It requires `androidx.compose.ui:ui-tooling` on the consumer's classpath.
- Sentinel ships as `debugImplementation` with a `sentinel-no-op` release
  counterpart, so a debug-only, reflection-based approach is appropriate here.

Also confirmed in the existing codebase:

- `DomainComponent.kt:251` already registers
  `Application.ActivityLifecycleCallbacks`. The overlay controller hooks in at
  the same place.
- `InMemoryBundlesCache` and `PreferenceCache` are the precedent for an
  in-memory data source.
- `sentinel/src/main/AndroidManifest.xml` declares `POST_NOTIFICATIONS` and no
  `INTERNET`.
- `generateReadme` only substitutes version strings into the hand-written
  `README.md`. Editing the README directly is safe.
- `compareSentinelApis` diffs only the `Sentinel`, `SentinelInitializer` and
  `Sentinel$Tool` blocks. New public classes are governed by the checked-in
  binary-compatibility-validator dumps instead.

## Architecture

### Package layout

Mirrors the existing `ui/bundles` and `ui/crash` structure.

```
data/models/local/annotations/     Annotation, AnnotationTarget
data/sources/memory/annotations/   AnnotationCache, InMemoryAnnotationCache
domain/annotations/                AnnotationsRepository, exporters
ui/annotations/                    AnnotationsActivity, Fragment, ViewModel, State, Event, adapter
ui/annotations/overlay/            AnnotationOverlayController, AnnotationOverlayView, resolvers
ui/tools/AnnotationsTool.kt        Public tool, appears in the Tools list
```

Dependency injection follows the existing kotlin-inject components: the cache is
provided by `DataComponent` under `@DataScope`, `AnnotationOverlayController` is
started from `DomainComponent.setup()` next to `initializeBundleMonitor()`, and
`AnnotationsViewModel` is registered in `ViewModelComponent` with the same
`@IntoMap` boilerplate the other view models use.

### Store

`AnnotationCache` holds the notes in memory for the process lifetime, matching
Borealis. It exposes the note list and an `active: StateFlow<Boolean>` capture
flag, and supports add, update note, remove, clear, rebind target, and mark
sent. Ids are short random alphanumeric strings, unique within the current list,
generated on add, as in `AnnotationStore._generateId`.

Notes do not survive process death. This is a deliberate limitation carried over
from Borealis: a note's value is tied to a live element it can be re-bound to.

### Capture overlay

`AnnotationOverlayController` registers `Application.ActivityLifecycleCallbacks`,
tracks the resumed Activity, and adds an `AnnotationOverlayView` to
`activity.window.decorView` as the last child with `MATCH_PARENT`. On an activity
change it detaches from the old Activity and attaches to the new one. The store
outlives the overlay.

Three states, driven by the capture flag and the note count:

| State | Overlay | Touch handling |
|---|---|---|
| Idle: no notes, capture off | Not attached | Not applicable |
| Passive: notes exist, capture off | Attached, draws markers | `dispatchTouchEvent` returns true only when the DOWN event lands in a badge rect. Everything else falls through and the app stays usable. |
| Capture: capture on | Attached, draws markers and the current selection | Consumes all touches |

Outlines and numbered badges are painted in `onDraw` on a single Canvas, which is
cheaper than Borealis's per-marker widget stack. The inspect info card is a real
child View because it carries a button. The note input is a `BottomSheetDialog`.

The overlay renders inside the host Activity, whose theme is arbitrary and may
not be an AppCompat or Material theme. Every view and dialog the overlay inflates
is therefore built against a `ContextThemeWrapper(activity, R.style.Sentinel_Theme)`
rather than the Activity context directly, so the overlay looks the same in every
host app and cannot crash on a host theme that lacks the Material attributes
`BottomSheetDialog` requires.

Marker positions are recomputed by a `ViewTreeObserver.OnPreDrawListener` on the
decor view, which invalidates only when something moved. This fires on real draws
only, so it is cheaper than Borealis's re-arming post-frame callback, and it
handles scrolling, keyboard insets and rotation without special cases.

Borealis turns its floating debug button into a close button during capture.
Sentinel has no floating button, so the overlay owns a small draggable close
control while capture is on. A long press on it calls `Sentinel.show()`, which
reopens the Sentinel panel with capture still armed, mirroring Borealis.

Entry flow: Tools list, then `AnnotationsActivity`, then "Start annotating",
which sets the capture flag and finishes the Sentinel activity so the user lands
back on the live app with capture armed.

### Target resolution

One interface with two implementations:

```kotlin
internal interface TargetResolver {
    fun resolve(root: View, point: PointF): AnnotationTarget?
    fun stepUp(target: AnnotationTarget): AnnotationTarget?
    fun ordinalOf(target: AnnotationTarget): Pair<Int, Int>
    fun rebind(annotation: Annotation, root: View): AnnotationTarget?
}
```

A composite delegates to `ComposeTargetResolver` when the hit lands inside an
`AndroidComposeView`, and to `ViewTargetResolver` otherwise.

**Views.** Descend from the decor view in reverse draw order, honouring
`getChildDrawingOrder` when `isChildrenDrawingOrderEnabled` is set. Skip views
that are not `VISIBLE` and views with zero size. The deepest hit wins, then walk
up to the nearest annotatable ancestor. Tapping a spot that resolves to the
already-selected element broadens the selection one step up and wraps back to
the deepest target past the outermost one, as `stepUp` does in Borealis.

A view is annotatable when any of these hold:

- `resources.getResourcePackageName(id) == context.packageName`, which is the
  Android equivalent of Borealis's `debugIsWidgetLocalCreation` test for "the
  developer wrote this themselves"
- it is a leaf content view: `TextView`, `ImageView`, `EditText`,
  `CompoundButton`, `ButtonStyleButton` and similar
- it is clickable

Framework chrome is excluded: `DecorView`, `ActionBarOverlayLayout`,
`ContentFrameLayout`, and anything under `com.android.internal`.

**Compose.** Build the `Group` tree through the Radiography recipe above.
Descend by `group.box` containment to the deepest group that has a `location`
whose `sourceFile` belongs to the application. Its `name` is the composable name
and `"${sourceFile}:${lineNumber}"` is the source location. Cross-reference
`semanticsOwner.getAllSemanticsNodes(mergingEnabled = false)` by bounds to pick
up `testTag`, `Text`, `ContentDescription` and `Role`.

On `LinkageError`, or when `ui-tooling-data` is absent, fall back to the View
resolver so the `ComposeView` itself remains annotatable with a null source
location, and surface a single "unsupported Compose version" line in the
annotations screen.

**Cost.** Tree walks happen on tap and on rebind attempts only. The per-draw
listener recomputes bounds of already-bound targets through
`getLocationOnScreen` and cached `LayoutInfo` bounds, with no traversal per
frame. Borealis re-probes stale annotations every frame; this port does not.

**Screen identity.** `screen` is the resumed Activity's `simpleName`. `fragment`
is the deepest `Fragment` whose view contains the target, found by walking
`supportFragmentManager` recursively; it is the analogue of Borealis's
`screenWidget`. `route` is the nearest `NavBackStackEntry` destination route when
androidx-navigation is on the classpath, resolved reflectively, and null
otherwise.

**Rebind after navigation.** For each annotation whose bound target is gone, hit
test the saved rect centre against the new root. Accept the result only when the
screen identity matches and either the `viewId` matches or, when the element has
no id, the type, ordinal and text all match. Fields that did not resolve on
either side are treated permissively, so a missing field never orphans a note.
This mirrors `_matchesCapturedIdentity` in Borealis.

### Export payload

One schema covers both toolkits so a single routine prompt handles either.
Serialized with kotlinx.serialization, already a core dependency.

```json
{
  "id": "a3k9x2m1",
  "note": "button too small",
  "toolkit": "view",
  "targetType": "MaterialButton",
  "targetPath": "CoordinatorLayout>MaterialCardView>MaterialButton",
  "viewId": "submitButton",
  "testTag": null,
  "sourceLocation": null,
  "ordinal": 1,
  "ordinalCount": 3,
  "text": "Submit",
  "screen": "MainActivity",
  "fragment": "CheckoutFragment",
  "route": null
}
```

A Compose target sets `toolkit` to `"compose"`, fills `targetType` with the
composable name, `sourceLocation` with `"CheckoutScreen.kt:142"` and `testTag`
where one is set, and leaves `viewId` null.

`viewId` is a stronger locator than the Flutter equivalent. Borealis relies on
`widgetKey`, which most widgets do not have. Most Android views carry an id, and
`getResourceEntryName` yields a string an agent can grep for as both
`R.id.submitButton` and `@+id/submitButton`.

### Exporters

```kotlin
public interface AnnotationExporter {
    @DrawableRes public fun icon(): Int
    @StringRes public fun label(): Int
    public suspend fun export(annotations: List<Annotation>): String
}
```

`export` returns a short status message for a Snackbar and throws to signal
failure, as in Borealis.

Borealis's `buildExporterOptions(context)` is deliberately not ported. Neither
shipped Borealis exporter overrides it. It can be added when a destination
picker actually needs it.

`Annotation` is public, because exporters receive the objects and custom
destinations control their own serialization. The live view or composition handle
on it is `internal` and is never serialized.

`ClipboardAnnotationExporter` is the default. It writes the JSON array through
`ClipboardManager.setPrimaryClip` and returns "Copied N annotations".

`ClaudeRoutineExporter` posts to the routine fire endpoint:

```
POST <fireUrl>
Authorization: Bearer <token>
anthropic-version: 2023-06-01
anthropic-beta: experimental-cc-routine-2026-04-01
Content-Type: application/json

{"text": "[ ...annotation JSON array... ]"}
```

It uses `HttpURLConnection` on `Dispatchers.IO` with connect and read timeouts,
and throws with the status code and response body on a non-2xx response. OkHttp
is not used: `sentinel` core has no HTTP dependency today and one POST does not
justify adding one for every consumer.

Export sends only notes that are not yet sent, then marks them sent, and the
list dims them. This carries `isSent` and `markAsSent` over unchanged.

### Token handling

The Claude Code OAuth token is a long-lived credential carrying the developer's
full permissions. Borealis takes it as a plain `String`, and
`example/lib/main.dart:51` in that repository currently holds a real one in
version control. Baked into an APK it is extractable by anyone with the file.

Two changes from the Borealis version:

1. `ClaudeRoutineExporter` takes `token: () -> String?` rather than
   `token: String`, so an app can read it from `BuildConfig` or `local.properties`
   or a development-only runtime source instead of hardcoding a literal.
2. Export refuses with a clear message when the provider returns null or blank,
   rather than firing an unauthenticated request.

### Configuration

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

`AnnotationsTool()` with no exporter defaults to the clipboard. It joins
`DEFAULT_TOOLS`, so the feature ships built in with no setup, and a
user-supplied instance replaces the default, deduped by type the way
`LibraryComponents.setup` already special-cases `CertificateTool`.

### Manifest

Sending to Claude requires adding `INTERNET` to
`sentinel/src/main/AndroidManifest.xml`. Library manifests merge into the host
app. The precedent is `POST_NOTIFICATIONS`, which already merges this way, and
Sentinel is `debugImplementation` only with `sentinel-no-op` used for release
builds. The addition is documented in the README rather than left to be
discovered.

## Annotations screen

`AnnotationsActivity`, `AnnotationsFragment` and `AnnotationsViewModel` follow
`BundlesActivity` exactly: `BaseChildActivity`, `BaseChildFragment`,
`BaseChildViewModel<State, Event>` and the `viewBinding` delegate. The manifest
entry sits alongside the others with `exported="false"`,
`taskAffinity="com.infinum.sentinel"`, `theme="@style/Sentinel.Theme.Leaf"` and
the `sentinel_infinum_monitored=false` meta-data. All new resources carry the
enforced `sentinel_` prefix.

The contents port directly from `AnnotationBorealisPage`:

- Empty state: an "Enable annotations" button and a line of hint text.
- Otherwise a row with "Start annotating" and the export button, whose icon and
  label come from the exporter, showing a spinner while the export runs.
- A `RecyclerView` of notes. Each row shows the index, the note text, the target
  type, a subtitle with `viewId` or `sourceLocation`, a "Sent" chip once sent,
  and a delete action behind a confirmation dialog.
- A `clear` action in the toolbar that removes all notes.
- A Snackbar carrying the exporter's status message, or `Export failed: ...`.

The note input is a `BottomSheetDialog` owned by the overlay, titled
`Note for <type>`, with a multiline field and Cancel and Save actions, plus
Delete when editing an existing note. Saving empty text while editing removes the
note, matching Borealis.

## No-op module

`sentinel-no-op` gains inert stubs for every new public type: `AnnotationsTool`,
`AnnotationExporter`, `ClipboardAnnotationExporter`, `ClaudeRoutineExporter` and
`Annotation`. The existing `CertificateTool` stub is the template.

## Build gates

All of these already exist in the repository and all are hard failures:

- `-Xexplicit-api=strict` requires explicit visibility and return types on every
  public declaration.
- `apiDump` must be re-run. `sentinel/api/sentinel.api` and
  `sentinel-no-op/api/sentinel-no-op.api` are checked in.
- `compareSentinelApis` diffs the `Sentinel`, `SentinelInitializer` and
  `Sentinel$Tool` blocks between the two modules.
- `detekt` and Android `lint` must pass.

## Testing

Unit tests in `src/test`, covering logic that needs no device:

- ordinal sorting, top to bottom then left to right
- the annotatable filter
- id generation and uniqueness
- export JSON serialization for both toolkits
- rebind identity rules, including the permissive treatment of unresolved fields
- hit-test ordering, the analogue of Borealis's pure `firstAnnotationTarget` test

Instrumented tests in `src/androidTest`, where Espresso and fragment-testing are
already wired:

- overlay attach and detach across the activity lifecycle
- capture versus passive touch routing
- resolution against a real layout
- marker rects after a scroll

The Compose reflection layer cannot be meaningfully unit tested. The mitigation
is to keep it thin: reflection only fetches the `Group` tree and the
`SemanticsOwner`, while all selection and filtering logic sits behind
`TargetResolver` and is tested with fakes.

The `sample` module has no Compose today, so the Compose path would otherwise
ship unexercised. One Compose screen is added to `sample` to validate it end to
end. This adds Compose to the sample module, which is debug-only and not part of
the published artifacts.

## Documentation

- README: `AnnotationsTool` in the built-in tools list, and a new "Annotations"
  section covering capture, markers, export, the `ui-tooling` requirement for
  Compose source locations, the added `INTERNET` permission, and the requirement
  to treat the Claude token as a secret.
- CHANGELOG entry.
- `claude-annotations-instructions.md` at the repository root, ported for
  Android: the field table uses `viewId`, `testTag`, `sourceLocation`, `toolkit`
  and `fragment`; grouping is by `screen` then `fragment`; the fix subagent
  verifies with `./gradlew detekt lint` instead of `flutter analyze`. The rest of
  the workflow, opening a pull request and answering its comments, carries over
  unchanged.

## Out of scope for v1

Recorded here so these are known limits rather than later surprises:

- Dialogs and popup windows. They are separate windows, and the overlay attaches
  to the Activity's window. Radiography's `WindowManagerGlobal` enumeration is
  the extension path.
- Multi-window and cross-process targets.
- Persistence of notes across process death.
- Right-to-left specific ordinal ordering.
