package com.infinum.sentinel.ui.deeplinks.list

import androidx.annotation.StringRes
import com.infinum.sentinel.R

/**
 * An entry of a row's overflow menu, in menu order, with its label.
 */
internal enum class LinkRowAction(
    @StringRes val labelRes: Int,
) {
    EDIT(R.string.sentinel_deep_links_menu_edit),
    ADD_PARAMETER(R.string.sentinel_deep_links_menu_add_parameter),
    RENAME(R.string.sentinel_deep_links_menu_rename),
    DUPLICATE(R.string.sentinel_deep_links_menu_duplicate),
    BREAKDOWN(R.string.sentinel_deep_links_menu_breakdown),
    COPY(R.string.sentinel_deep_links_menu_copy),
    SHARE(R.string.sentinel_deep_links_menu_share),
    DELETE(R.string.sentinel_deep_links_menu_delete),
    ;

    companion object {
        /**
         * The entries [row]'s menu offers. A base link has Breakdown, Copy and Share; a declared template, which
         * isn't a real link, has Breakdown only. A variant has every entry.
         */
        fun forRow(row: DeepLinksItem.LinkRow): List<LinkRowAction> =
            when {
                row is DeepLinksItem.VariantRow -> entries
                row.launchLink == null -> listOf(BREAKDOWN)
                else -> listOf(BREAKDOWN, COPY, SHARE)
            }
    }
}
