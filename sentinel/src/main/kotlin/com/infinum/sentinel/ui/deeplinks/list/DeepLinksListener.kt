package com.infinum.sentinel.ui.deeplinks.list

import android.view.View
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice

/**
 * Clicks in the Deep links list. Every callback gets the immutable row it came from, plus the anchor view when
 * the click shows a popup. Don't keep a row or a view after the callback returns.
 */
internal interface DeepLinksListener {
    /** The row was tapped. It opens the breakdown of [DeepLinksItem.LinkRow.link]. */
    fun onRowClick(row: DeepLinksItem.LinkRow)

    /** Launch was tapped. It is shown only when [DeepLinksItem.LinkRow.launchLink] isn't null. */
    fun onLaunchClick(
        row: DeepLinksItem.LinkRow,
        anchor: View,
    )

    /** The overflow button was tapped. */
    fun onMoreClick(
        row: DeepLinksItem.LinkRow,
        anchor: View,
    )

    /** Add variant was tapped. It is disabled when the base link has no spec. */
    fun onAddVariantClick(row: DeepLinksItem.BaseLinkRow)

    /** The verification status chip of a row was tapped (web links on Android 12 and later). */
    fun onStatusClick(row: DeepLinksItem.LinkRow)

    /** The action button of a notice was tapped (only the link-handling-off notice has one). */
    fun onNoticeActionClick(notice: DeepLinksNotice)
}
