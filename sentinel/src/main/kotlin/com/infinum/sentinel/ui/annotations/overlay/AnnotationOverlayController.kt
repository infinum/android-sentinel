package com.infinum.sentinel.ui.annotations.overlay

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.compose.ui.tooling.data.UiToolingDataApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.infinum.sentinel.data.models.local.annotations.Annotation
import com.infinum.sentinel.data.models.local.annotations.AnnotationToolkit
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
 * scrolling, keyboard insets and rotation without special cases. [detach] removes that listener
 * again — otherwise every [attach] would leave one more of them firing on that Activity's
 * `ViewTreeObserver`, forever, even after the overlay itself was gone.
 */
internal class AnnotationOverlayController(
    private val cache: AnnotationCache,
) : Application.ActivityLifecycleCallbacks {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val resolver = CompositeTargetResolver()

    private var activity: Activity? = null
    private var overlay: AnnotationOverlayView? = null
    private var preDrawListener: ViewTreeObserver.OnPreDrawListener? = null
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
        val listener =
            ViewTreeObserver.OnPreDrawListener {
                refreshMarkers(cache.load().value)
                true
            }
        host.window.decorView.viewTreeObserver.addOnPreDrawListener(listener)
        preDrawListener = listener
        overlay = view
        return view
    }

    /**
     * Removes the overlay view and the pre-draw listener [attach] added for it. The listener is
     * removed from the overlay's own `viewTreeObserver` while the overlay is still attached — at
     * this point that observer is the same live instance [attach] registered on, and is still
     * alive — so this is what actually frees the host Activity of this controller's per-frame
     * work once neither capture nor a saved note needs the overlay anymore.
     */
    private fun detach() {
        val view = overlay ?: return
        preDrawListener?.let { listener ->
            val observer = view.viewTreeObserver
            if (observer.isAlive) observer.removeOnPreDrawListener(listener)
        }
        preDrawListener = null
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
                stepUpTarget(current) ?: fresh
            } else {
                fresh
            }

        selected = next
        overlay?.selection = next.node.bounds
    }

    /**
     * [current] broadened one annotatable step toward the root, or `null` when it is already the
     * outermost annotatable node — the caller then wraps back to the deepest target under the
     * tap.
     *
     * The stepped-to node needs its own live handle: reusing [current]'s handle would leave a
     * note, once saved, pointing at — and its marker drawn around — the narrower element first
     * tapped rather than the one the user actually broadened to.
     */
    @Suppress("ReturnCount")
    private fun stepUpTarget(current: ResolvedTarget): ResolvedTarget? {
        val stepped = TargetSelection.stepUp(current.node) ?: return null
        val handle = handleOf(stepped) ?: return null
        return ResolvedTarget(stepped, handle, current.toolkit)
    }

    /** The live element behind [node] — the [View] or Compose `Group`, however it was reached. */
    @OptIn(UiToolingDataApi::class)
    private fun handleOf(node: TargetNode): Any? =
        when (node) {
            is ViewTargetNode -> node.view
            is ComposeTargetNode -> node.group
            else -> null
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
            if (target.toolkit == AnnotationToolkit.COMPOSE) {
                ComposeSupport.rootNodeOf(root)
            } else {
                root.toTargetNode(parent = null)
            }
        val (ordinal, ordinalCount) = tree?.let { TargetSelection.ordinalOf(it, target.node) } ?: (1 to 1)

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
     * recreated. Only bounds are read here — no tree walk — so this stays cheap per frame.
     */
    private fun refreshMarkers(annotations: List<Annotation>) {
        val host = activity ?: return
        val view = overlay ?: return
        val root = host.window.decorView

        val markers =
            annotations.withIndex().mapNotNull { (index, annotation) ->
                val onThisScreen = annotation.screen == null || annotation.screen == host.javaClass.simpleName
                val bounds = if (onThisScreen) boundsOf(annotation, root) else null
                bounds?.let { AnnotationMarker(index + 1, it, annotation) }
            }

        if (view.markers != markers) view.markers = markers
    }

    /**
     * Bounds of [annotation]'s live element, without walking the tree when it can be avoided.
     *
     * A live, attached View handle is read directly — that is what keeps the per-frame refresh
     * in [refreshMarkers] cheap. Only when the handle is stale (unset, detached, or a Compose
     * group, which [ComposeTargetResolver] never hands back as reusable across recomposition)
     * does this fall back to [TargetResolver.rebind], which does walk the tree; that cost is
     * paid only for the annotations that actually need it.
     */
    @Suppress("ReturnCount")
    private fun boundsOf(annotation: Annotation, root: View): Bounds? {
        val liveHandle = annotation.target as? View
        if (liveHandle != null && liveHandle.isAttachedToWindow && liveHandle.width > 0) {
            val location = IntArray(2)
            liveHandle.getLocationOnScreen(location)
            return Bounds(location[0], location[1], location[0] + liveHandle.width, location[1] + liveHandle.height)
        }
        val rebound = resolver.rebind(annotation, root) ?: return null
        scope.launch { cache.rebind(annotation.id, rebound.handle) }
        return rebound.node.bounds
    }
}
