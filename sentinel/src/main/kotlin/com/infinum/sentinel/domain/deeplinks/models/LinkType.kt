package com.infinum.sentinel.domain.deeplinks.models

/**
 * Kind of a deep link, derived from its scheme.
 */
internal enum class LinkType {
    CUSTOM_SCHEME,
    WEB,
    ;

    internal companion object {
        private const val SCHEME_HTTP = "http"
        private const val SCHEME_HTTPS = "https"

        /**
         * Returns [WEB] for `http` and `https` (case-insensitive), [CUSTOM_SCHEME] for anything else, including null.
         */
        internal fun from(scheme: String?): LinkType =
            if (scheme.equals(SCHEME_HTTP, ignoreCase = true) || scheme.equals(SCHEME_HTTPS, ignoreCase = true)) {
                WEB
            } else {
                CUSTOM_SCHEME
            }
    }
}
