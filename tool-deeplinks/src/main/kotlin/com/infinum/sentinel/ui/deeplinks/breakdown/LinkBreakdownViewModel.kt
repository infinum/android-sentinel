package com.infinum.sentinel.ui.deeplinks.breakdown

import com.infinum.sentinel.ui.deeplinks.DeepLinksBaseViewModel

internal class LinkBreakdownViewModel : DeepLinksBaseViewModel<LinkBreakdownState, Nothing>() {
    private var link: String = ""

    /** Sets the link to break down. Call before [data]. */
    fun setLink(value: String) {
        link = value
    }

    override fun data() = setState(LinkBreakdownState.Data(LinkBreakdownMapper.map(link)))
}
