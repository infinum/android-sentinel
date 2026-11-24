package com.infinum.sentinel.ui.deeplinks.list

import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice

/**
 * Everything the Deep links list reports back. Rows are passed whole, as the view holders do.
 */
internal data class DeepLinksListCallbacks(
    val onRowClick: (DeepLinksItem.LinkRow) -> Unit,
    val onLaunchClick: (DeepLinksItem.LinkRow) -> Unit,
    val onMoreClick: (DeepLinksItem.LinkRow) -> Unit,
    val onAddVariantClick: (DeepLinksItem.BaseLinkRow) -> Unit,
    val onStatusClick: (DeepLinksItem.LinkRow) -> Unit,
    val onNoticeActionClick: (DeepLinksNotice) -> Unit,
)
