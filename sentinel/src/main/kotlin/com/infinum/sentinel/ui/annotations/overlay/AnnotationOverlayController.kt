package com.infinum.sentinel.ui.annotations.overlay

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.infinum.sentinel.annotations.SentinelAnnotation
import com.infinum.sentinel.data.sources.memory.annotations.AnnotationCache
import com.infinum.sentinel.extensions.isSentinelOwnScreen
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
 * scrolling, keyboard insets and rotation without special cases. [detach] removes that listener.
 *
 * Sentinel's own screens are never hosts: see [Activity.isSentinelOwnScreen].
 */
internal class AnnotationOverlayController(
    private val cache: AnnotationCache,
) : Application.ActivityLifecycleCallbacks {
    private companion object {
        private const val STOP_BUTTON_MARGIN = 48
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val resolver = CompositeTargetResolver()

    private var activity: Activity? = null
    private var overlay: AnnotationOverlayView? = null
    private var card: AnnotationInspectCard? = null
    private var stopButton: AnnotationStopButton? = null
    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private var selected: ResolvedTarget? = null

    /**
     * Identifies the element the current selection chain started from, so repeated taps on it keep
     * broadening instead of resetting. See [anchorOf].
     */
    private var selectionAnchor: Any? = null

    /**
     * Ids of the notes whose [TargetResolver.rebind] already failed during the current layout
     * pass, so [refreshMarkers], which runs on every frame, does not walk the tree for them again.
     * Cleared on each global layout and by [sync] whenever the stored notes change.
     */
    private val failedRebinds = mutableSetOf<String>()

    /** The open note sheet, the Activity it is shown on, and what it was opened for. */
    private var noteSheet: AnnotationNoteDialog.Sheet? = null
    private var noteHost: Activity? = null
    private var noteRequest: NoteRequest? = null

    /** A note sheet closed because its Activity is being recreated, reopened on the new instance. */
    private var pendingNote: NoteRequest? = null

    /**
     * What a note sheet edits. [onSave] gets `restored = true` when the sheet was reopened after
     * its Activity was recreated, so the live handle captured before is no longer valid.
     */
    private data class NoteRequest(
        val targetType: String,
        val initial: String?,
        val draft: String? = null,
        val restored: Boolean = false,
        val onSave: (note: String, restored: Boolean) -> Unit,
        val onDelete: () -> Unit,
    )

    fun start(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        combine(cache.active, cache.load()) { active, annotations -> active to annotations }
            .onEach { (active, annotations) -> sync(active, annotations) }
            .launchIn(scope)
    }

    /**
     * On API 29+ in multi-window, two Activities can be resumed at once and the previous host is
     * not paused. So the overlay is detached whenever a different Activity resumes, then attached
     * to the new one by [sync].
     */
    override fun onActivityResumed(activity: Activity) {
        if (activity.isSentinelOwnScreen) return
        if (this.activity !== activity) detach()
        this.activity = activity
        sync(cache.active.value, cache.load().value)
        pendingNote?.let { request ->
            pendingNote = null
            showNote(activity, request)
        }
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

    /**
     * Drops the live handles that belonged to [activity]. [SentinelAnnotation.target] lives as long
     * as the process, and a `View` handle holds its Activity through its context. [boundsOf]
     * re-binds the note the next time its screen is on top.
     *
     * An open note sheet is closed too, so its window does not leak. When the Activity is only
     * being recreated, for example on rotation, the sheet reopens with its draft on the new
     * instance.
     */
    override fun onActivityDestroyed(activity: Activity) {
        if (noteHost === activity) {
            val sheet = noteSheet
            val request = noteRequest
            clearNoteSheet()
            if (sheet != null && request != null && activity.isChangingConfigurations) {
                pendingNote = request.copy(draft = sheet.draft, restored = true)
            }
            sheet?.dismissSilently()
        }
        if (this.activity === activity) {
            detach()
            this.activity = null
        }
        cache.releaseTargets { annotation -> annotation.isHostedBy(activity) }
    }

    /**
     * Whether [activity] owns this note's live handle.
     *
     * A `View` handle is decided precisely, by walking its context chain: a View's context is
     * often a `ContextThemeWrapper` around the Activity rather than the Activity itself. A
     * Compose `Group` handle exposes no such chain, so it falls back to the screen name the note
     * recorded. That can release a handle a same-named Activity still owns, which rebind restores.
     */
    private fun SentinelAnnotation.isHostedBy(activity: Activity): Boolean {
        val handle = target ?: return false
        if (handle !is View) return screen == activity.javaClass.simpleName
        var context: Context? = handle.context
        while (context is ContextWrapper) {
            if (context === activity) return true
            context = context.baseContext
        }
        return false
    }

    private fun sync(active: Boolean, annotations: List<SentinelAnnotation>) {
        val host = activity ?: return
        if (!active && annotations.isEmpty()) {
            detach()
            return
        }
        val view = overlay ?: attach(host)
        view.captureActive = active
        stopButton?.visibility = AnnotationStopButton.visibilityFor(active)
        if (!active) {
            selected = null
            selectionAnchor = null
            card?.hide()
        }
        // The stored notes changed, so a rebind that failed before may now succeed.
        failedRebinds.clear()
        refreshMarkers(annotations)
    }

    private fun attach(host: Activity): AnnotationOverlayView {
        val decor = host.window.decorView as ViewGroup
        val view =
            AnnotationOverlayView(host).apply {
                onCaptureTap = { x, y -> handleCaptureTap(host, x, y) }
                onBadgeTapped = { annotation -> editNote(host, annotation) }
            }
        decor.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        overlay = view

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
            ).apply { setMargins(0, 0, STOP_BUTTON_MARGIN, STOP_BUTTON_MARGIN) },
        )
        stopButton = stop

        val listener =
            ViewTreeObserver.OnPreDrawListener {
                refreshMarkers(cache.load().value)
                true
            }
        host.window.decorView.viewTreeObserver.addOnPreDrawListener(listener)
        preDrawListener = listener

        val onLayout = ViewTreeObserver.OnGlobalLayoutListener { failedRebinds.clear() }
        host.window.decorView.viewTreeObserver.addOnGlobalLayoutListener(onLayout)
        globalLayoutListener = onLayout
        return view
    }

    /**
     * Removes the overlay view and the pre-draw listener [attach] added for it. The listener is
     * removed while the overlay is still attached, because only then is its `viewTreeObserver` the
     * live instance [attach] registered on.
     */
    private fun detach() {
        val view = overlay ?: return
        val observer = view.viewTreeObserver
        preDrawListener?.let { listener ->
            if (observer.isAlive) observer.removeOnPreDrawListener(listener)
        }
        preDrawListener = null
        globalLayoutListener?.let { listener ->
            if (observer.isAlive) observer.removeOnGlobalLayoutListener(listener)
        }
        globalLayoutListener = null
        failedRebinds.clear()
        listOfNotNull(overlay, card, stopButton).forEach { (it.parent as? ViewGroup)?.removeView(it) }
        overlay = null
        card = null
        stopButton = null
        selected = null
        selectionAnchor = null
    }

    /**
     * A tap on the already-selected element broadens the selection one step toward the screen
     * root, and wraps back to the deepest target past the outermost one. Anything else selects
     * afresh.
     *
     * Selecting never opens the note sheet: the second tap would then land on the sheet instead of
     * the element, and broadening would be unreachable. The inspect card's "Add note" opens it.
     */
    private fun handleCaptureTap(host: Activity, x: Int, y: Int) {
        val root = host.window.decorView
        val fresh = resolver.resolve(root, x, y) ?: return
        val current = selected

        // Compared against where the chain started: `resolve` always returns the deepest target,
        // so after one step up it never equals the current selection.
        val anchor = anchorOf(fresh)
        val next =
            if (current != null && anchor == selectionAnchor) {
                stepUpTarget(current) ?: fresh
            } else {
                selectionAnchor = anchor
                fresh
            }

        selected = next
        overlay?.selection = next.node.bounds

        val decorBounds = boundsOfView(host.window.decorView)
        card?.showFor(next.node, decorBounds)
    }

    /**
     * A key that stays equal across taps on the same element. A `View` is the same object on
     * every tap. Compose rebuilds its tooling `Group` objects on every read, so a composable is
     * keyed by its type, source location and bounds instead.
     */
    private fun anchorOf(target: ResolvedTarget): Any =
        when (val handle = target.handle) {
            is View -> handle
            else -> Triple(target.node.type, target.node.sourceLocation, target.node.bounds)
        }

    private fun boundsOfView(view: View): Bounds {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return Bounds(location[0], location[1], location[0] + view.width, location[1] + view.height)
    }

    /**
     * [current] broadened one annotatable step toward the root, or `null` when it is already the
     * outermost annotatable node. The caller then wraps back to the deepest target under the tap.
     *
     * The returned target carries the live handle of the broadened node, not of [current], so a
     * saved note points at the element the user broadened to.
     */
    @Suppress("ReturnCount")
    private fun stepUpTarget(current: ResolvedTarget): ResolvedTarget? {
        val stepped = TargetSelection.stepUp(current.node) ?: return null
        val handle = handleOf(stepped) ?: return null
        return ResolvedTarget(stepped, handle, current.toolkit)
    }

    /** The live element behind [node]: the [View] or Compose `Group`. */
    @OptIn(UiToolingDataApi::class)
    private fun handleOf(node: TargetNode): Any? =
        when (node) {
            is ViewTargetNode -> node.view
            is ComposeTargetNode -> node.group
            else -> null
        }

    /** Opens the note sheet for the current selection. Called by the inspect card. */
    fun addNoteForSelection() {
        val host = activity ?: return
        val target = selected ?: return
        // Captured now, while the target's tree and Activity are live, so saving still works after
        // the sheet was reopened on a recreated Activity.
        val captured = captureAnnotation(host, target)
        showNote(
            host,
            NoteRequest(
                targetType = target.node.type,
                initial = null,
                onSave = { note, restored -> saveNote(captured, target, note, restored) },
                onDelete = { },
            ),
        )
    }

    private fun captureAnnotation(host: Activity, target: ResolvedTarget): SentinelAnnotation {
        // Counted against the target's own tree: ordinalOf finds the target by node identity, so a
        // freshly built tree would never contain it.
        val (ordinal, ordinalCount) =
            TargetSelection.ordinalOf(TargetSelection.rootOf(target.node), target.node)
        return SentinelAnnotation(
            id = "",
            note = "",
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
        )
    }

    /**
     * Stores the note. After a recreation the captured handle belongs to the destroyed Activity,
     * so it is left unset and [boundsOf] re-binds the note on the new screen.
     */
    private fun saveNote(captured: SentinelAnnotation, target: ResolvedTarget, note: String, restored: Boolean) {
        scope.launch {
            val stored = cache.add(captured.copy(note = note))
            if (!restored) stored.target = target.handle
            selected = null
            selectionAnchor = null
            overlay?.selection = null
        }
    }

    private fun editNote(host: Activity, annotation: SentinelAnnotation) {
        showNote(
            host,
            NoteRequest(
                targetType = annotation.targetType,
                initial = annotation.note,
                onSave = { note, _ -> scope.launch { cache.updateNote(annotation.id, note) } },
                onDelete = { scope.launch { cache.remove(annotation.id) } },
            ),
        )
    }

    private fun showNote(host: Activity, request: NoteRequest) {
        noteSheet?.dismissSilently()
        noteHost = host
        noteRequest = request
        noteSheet =
            AnnotationNoteDialog.show(
                context = host,
                targetType = request.targetType,
                initial = request.initial,
                draft = request.draft,
                onSave = { note -> request.onSave(note, request.restored) },
                onDelete = request.onDelete,
                onDismiss = ::clearNoteSheet,
            )
    }

    private fun clearNoteSheet() {
        noteSheet = null
        noteHost = null
        noteRequest = null
    }

    /** Simple name of the deepest Fragment whose view covers [bounds]. */
    private fun fragmentNameOf(host: Activity, bounds: Bounds): String? {
        if (host !is FragmentActivity) return null
        var deepest: Fragment? = null
        fun visit(fragments: List<Fragment>) {
            for (fragment in fragments) {
                val view = fragment.view ?: continue
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
     * recreated. Runs on every frame, so it reads bounds and avoids walking the tree.
     */
    private fun refreshMarkers(annotations: List<SentinelAnnotation>) {
        val host = activity ?: return
        val view = overlay ?: return
        val root = host.window.decorView

        val markers =
            annotations.withIndex().mapNotNull { (index, annotation) ->
                val onThisScreen = annotation.screen == null || annotation.screen == host.javaClass.simpleName
                val bounds = if (onThisScreen) boundsOf(annotation, host, root) else null
                bounds?.let { AnnotationMarker(index + 1, it, annotation) }
            }

        if (view.markers != markers) view.markers = markers
    }

    /**
     * Bounds of [annotation]'s live element, without walking the tree when it can be avoided.
     *
     * A live, attached View handle is read directly, which keeps the per-frame refresh in
     * [refreshMarkers] cheap. Only a stale handle (unset, detached, or a Compose group, which is
     * never reusable across recomposition) falls back to [TargetResolver.rebind], which walks the
     * tree.
     *
     * A rebound element must sit in the Fragment the note was captured in, so a note does not jump
     * to a lookalike element on another screen of a single-Activity app.
     */
    @Suppress("ReturnCount")
    private fun boundsOf(annotation: SentinelAnnotation, host: Activity, root: View): Bounds? {
        val liveHandle = annotation.target as? View
        if (liveHandle != null && liveHandle.isAttachedToWindow && liveHandle.width > 0) {
            return visibleBoundsOf(liveHandle, root)
        }
        if (annotation.id in failedRebinds) return null
        val rebound =
            resolver.rebind(annotation, root)
                ?.takeIf { annotation.fragment == null || fragmentNameOf(host, it.node.bounds) == annotation.fragment }
        if (rebound == null) {
            failedRebinds += annotation.id
            return null
        }
        scope.launch { cache.rebind(annotation.id, rebound.handle) }
        return (rebound.handle as? View)?.let { visibleBoundsOf(it, root) } ?: rebound.node.bounds
    }

    /**
     * The on-screen part of [view], or `null` when none of it is visible: hidden by itself or an
     * ancestor, or scrolled out of a clipping parent. Keeps markers, and their tap targets, off
     * content the user cannot see.
     */
    private fun visibleBoundsOf(view: View, root: View): Bounds? {
        if (!view.isShown) return null
        val visible = Rect()
        if (!view.getGlobalVisibleRect(visible)) return null
        // getGlobalVisibleRect is relative to the window's root view; markers use screen coordinates.
        val rootLocation = IntArray(2)
        root.getLocationOnScreen(rootLocation)
        return Bounds(
            visible.left + rootLocation[0],
            visible.top + rootLocation[1],
            visible.right + rootLocation[0],
            visible.bottom + rootLocation[1],
        )
    }
}
