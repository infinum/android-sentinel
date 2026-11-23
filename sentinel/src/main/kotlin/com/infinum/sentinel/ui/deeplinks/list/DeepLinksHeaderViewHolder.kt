package com.infinum.sentinel.ui.deeplinks.list

import android.text.TextUtils
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.databinding.SentinelItemHeaderBinding

/**
 * Shows a section header. It is marked as a heading so TalkBack can jump between sections.
 */
internal class DeepLinksHeaderViewHolder(
    private val binding: SentinelItemHeaderBinding,
) : RecyclerView.ViewHolder(binding.root) {
    init {
        ViewCompat.setAccessibilityHeading(binding.titleView, true)
        // The shared header layout was made for a short count. A class name can be long, so it gets at most half
        // the screen width, and both texts end with an ellipsis instead of being clipped.
        binding.titleView.ellipsize = TextUtils.TruncateAt.END
        binding.countView.ellipsize = TextUtils.TruncateAt.END
        binding.countView.maxWidth = binding.root.resources.displayMetrics.widthPixels / 2
    }

    fun bind(item: DeepLinksItem.SectionHeader) {
        binding.titleView.setText(item.titleRes)
        binding.countView.isVisible = false
    }

    /**
     * Shows the screen's title, and next to it the class name when the title is a label. A label alone, such as
     * "Deep link", reads like a category, while the class name makes clear that the section is a screen.
     */
    fun bind(item: DeepLinksItem.ScreenHeader) {
        val className = item.componentName.substringAfterLast('.')
        binding.titleView.text = item.title
        binding.countView.text = className
        binding.countView.isVisible = className != item.title
    }
}
