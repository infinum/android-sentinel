package com.infinum.sentinel.sample

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.infinum.sentinel.sample.databinding.ActivityDeepLinkBinding

/**
 * Sets up the screen shared by the sample's deep link activities: the toolbar [title], an up control that
 * does the same as Back, and padding for the system bars, because the screen draws edge to edge.
 */
internal fun AppCompatActivity.setUpDeepLinkScreen(
    binding: ActivityDeepLinkBinding,
    @StringRes title: Int,
) {
    binding.toolbar.setTitle(title)
    // Same as Back on purpose: it closes this screen and doesn't jump to the Deep links tool.
    binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        binding.appBar.updatePadding(left = bars.left, top = bars.top, right = bars.right)
        binding.scrollView.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
        insets
    }
}
