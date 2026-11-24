package com.infinum.sentinel.domain.deeplinks.models

/**
 * A base link as declared: scheme, host, port and path.
 */
internal data class LinkSpec(
    val scheme: String,
    val host: HostSpec,
    val port: Int?,
    val path: PathSpec,
) {
    internal companion object {
        /** Adds a leading slash to a non-empty path that lacks one, so it can't fuse with the host. */
        internal fun withLeadingSlash(value: String): String =
            if (value.isEmpty() || value.startsWith("/")) value else "/$value"
    }

    /**
     * Text shown to the user: the scheme and `://`, then the host (`*` for any host, `*.` before a
     * wildcard suffix), the port and the path. A path that lacks a leading slash gets one. A prefix path ends in `*`; a suffix path starts with `/` and `*`.
     * (No literal example here: a slash followed by a star would open a nested Kotlin comment.)
     */
    val displayText: String
        get() = "$scheme://" + hostText() + port?.let { ":$it" }.orEmpty() + pathText()

    /**
     * True when the link cannot be launched as is: wildcard host or any non-literal path.
     */
    val isTemplate: Boolean
        get() = isHostTemplate() || isPathTemplate()

    private fun isHostTemplate(): Boolean = host is HostSpec.AnyHost || host is HostSpec.Wildcard

    private fun isPathTemplate(): Boolean =
        when (path) {
            PathSpec.None, is PathSpec.Literal -> false
            is PathSpec.Prefix, is PathSpec.Pattern, is PathSpec.AdvancedPattern, is PathSpec.Suffix -> true
        }

    private fun hostText(): String =
        when (host) {
            HostSpec.None -> ""
            HostSpec.AnyHost -> "*"
            is HostSpec.Literal -> host.host
            is HostSpec.Wildcard -> "*.${host.suffix}"
        }

    private fun pathText(): String =
        when (path) {
            PathSpec.None -> ""
            is PathSpec.Literal -> withLeadingSlash(path.value)
            is PathSpec.Prefix -> withLeadingSlash(path.value) + "*"
            is PathSpec.Pattern -> withLeadingSlash(path.value)
            is PathSpec.AdvancedPattern -> withLeadingSlash(path.value)
            is PathSpec.Suffix -> "/*" + path.value
        }
}
