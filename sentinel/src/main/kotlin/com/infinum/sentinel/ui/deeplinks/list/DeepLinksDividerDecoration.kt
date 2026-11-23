package com.infinum.sentinel.ui.deeplinks.list

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

/**
 * Draws the theme's list divider above each group of rows. A group is a base link with its variants, or a variant
 * listed on its own in No longer declared or Saved variants. Rows inside a group and rows right below a section
 * header get no divider, so a base link and its variants read as one block.
 *
 * [itemAt] returns the item at an adapter position, or null when there is none, for example for NO_POSITION.
 */
internal class DeepLinksDividerDecoration(
    context: Context,
    private val itemAt: (Int) -> DeepLinksItem?,
) : RecyclerView.ItemDecoration() {
    private val divider: Drawable? = listDivider(context)
    private val bounds = Rect()

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val height = divider?.intrinsicHeight ?: 0
        outRect.set(0, if (hasDividerAbove(parent.getChildAdapterPosition(view))) height else 0, 0, 0)
    }

    override fun onDraw(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val drawable = divider ?: return
        parent.children
            .filter { hasDividerAbove(parent.getChildAdapterPosition(it)) }
            .forEach { child ->
                parent.getDecoratedBoundsWithMargins(child, bounds)
                val top = bounds.top + child.translationY.roundToInt()
                drawable.setBounds(parent.paddingLeft, top, parent.width - parent.paddingRight, top + drawable.intrinsicHeight)
                drawable.draw(canvas)
            }
    }

    private fun hasDividerAbove(position: Int): Boolean {
        val isGroupStart =
            when (val item = itemAt(position)) {
                is DeepLinksItem.BaseLinkRow -> true
                is DeepLinksItem.VariantRow -> item.baseLink == null
                else -> false
            }
        return isGroupStart && position > 0 && itemAt(position - 1) is DeepLinksItem.LinkRow
    }

    private fun listDivider(context: Context): Drawable? {
        val attributes = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        return try {
            attributes.getDrawable(0)
        } finally {
            attributes.recycle()
        }
    }
}
