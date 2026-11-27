package com.infinum.sentinel.ui.deeplinks.breakdown

internal sealed class LinkBreakdownState {
    data class Data(
        val breakdown: LinkBreakdown,
    ) : LinkBreakdownState()
}
