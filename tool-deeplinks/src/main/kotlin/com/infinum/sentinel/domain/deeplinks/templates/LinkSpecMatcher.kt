package com.infinum.sentinel.domain.deeplinks.templates

import com.infinum.sentinel.domain.deeplinks.models.LinkSpec

/**
 * Tells whether a link matches a declared link shape, the way Android would match it.
 */
internal interface LinkSpecMatcher {
    /**
     * Returns true when [link] matches [spec], false when it doesn't and null when this device
     * cannot tell. Never throws.
     */
    fun matches(
        spec: LinkSpec,
        link: String,
    ): Boolean?
}
