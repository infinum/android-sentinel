package com.infinum.sentinel.ui.deeplinks

import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksOverview

internal sealed class DeepLinksState {
    data class Data(
        val overview: DeepLinksOverview,
        val variantCount: Int,
        val isLinkSettingsAvailable: Boolean,
    ) : DeepLinksState()
}
