package com.infinum.sentinel.ui.annotations.overlay

import android.annotation.SuppressLint
import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelViewAnnotationInspectBinding

/**
 * The compact card that names the selected element and offers "Add note".
 *
 * Positioned just below the selection, flipped above it when there is no room, and clamped to
 * the viewport so it is never partly off screen. Built against Sentinel's own theme because the
 * host Activity's theme is arbitrary.
 *
 * Implements [SentinelOverlayView] so target resolution skips it and taps resolve to the app
 * underneath.
 */
@SuppressLint("ViewConstructor")
internal class AnnotationInspectCard(
    context: Context,
) : MaterialCardView(ContextThemeWrapper(context, R.style.Sentinel_Theme)),
    SentinelOverlayView {
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

        // layoutParams is null until the card is first attached, so fresh params are built then.
        val params = (layoutParams as? FrameLayout.LayoutParams) ?: FrameLayout.LayoutParams(0, 0)
        params.width = measuredWidth
        params.height = measuredHeight
        params.leftMargin = left - viewport.left
        params.topMargin = top - viewport.top
        layoutParams = params
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
