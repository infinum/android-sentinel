package com.infinum.sentinel.ui.deeplinks.launch

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.ListPopupWindow
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode

/**
 * Asks how to open a web link. Each mode shows its name and a short explanation, and a tap picks it at once.
 * There is no remembered choice: the tester decides on every launch.
 */
internal object LaunchModePopup {
    /** Shows the popup under [anchor], aligned to its end, and calls [onPick] with the chosen mode. */
    fun show(
        anchor: View,
        onPick: (LaunchMode) -> Unit,
    ) {
        val context = anchor.context
        val modes = LaunchMode.entries
        val adapter =
            object : ArrayAdapter<LaunchMode>(context, android.R.layout.simple_list_item_2, android.R.id.text1, modes) {
                override fun getView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup,
                ): View =
                    super.getView(position, convertView, parent).apply {
                        findViewById<TextView>(android.R.id.text1).setText(titleOf(modes[position]))
                        findViewById<TextView>(android.R.id.text2).setText(descriptionOf(modes[position]))
                    }
            }
        ListPopupWindow(context).apply {
            anchorView = anchor
            setAdapter(adapter)
            width =
                context.resources
                    .getDimensionPixelSize(R.dimen.sentinel_deep_links_launch_popup_width)
                    .coerceAtMost(anchor.rootView.width)
            setDropDownGravity(Gravity.END)
            isModal = true
            setOnItemClickListener { _, _, position, _ ->
                dismiss()
                onPick(modes[position])
            }
            show()
        }
    }

    @StringRes
    private fun titleOf(mode: LaunchMode): Int =
        when (mode) {
            LaunchMode.REAL_TAP -> R.string.sentinel_deep_links_real_tap
            LaunchMode.OPEN_IN_APP -> R.string.sentinel_deep_links_open_in_app
        }

    @StringRes
    private fun descriptionOf(mode: LaunchMode): Int =
        when (mode) {
            LaunchMode.REAL_TAP -> R.string.sentinel_deep_links_real_tap_description
            LaunchMode.OPEN_IN_APP -> R.string.sentinel_deep_links_open_in_app_description
        }
}
