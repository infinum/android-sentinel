package com.infinum.sentinel.ui.deeplinks.editor

import android.content.IntentFilter
import android.os.Build
import android.os.PatternMatcher
import androidx.core.net.toUri
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.templates.LinkSpecMatcher

/**
 * Matches links with the platform: builds an [IntentFilter] from the declared shape and asks
 * [IntentFilter.matchData], so the answer is the one Android would give.
 */
internal class AndroidLinkSpecMatcher : LinkSpecMatcher {
    override fun matches(
        spec: LinkSpec,
        link: String,
    ): Boolean? =
        try {
            buildFilter(spec)?.let { it.matchData(null, spec.scheme, link.toUri()) >= 0 }
        } catch (ignored: RuntimeException) {
            null
        }

    /** Returns null when the path kind isn't supported on this device. */
    private fun buildFilter(spec: LinkSpec): IntentFilter? {
        val filter = IntentFilter().apply { addDataScheme(spec.scheme) }
        val port = spec.port?.toString()
        val authority =
            when (val host = spec.host) {
                HostSpec.None -> null
                HostSpec.AnyHost -> "*"
                is HostSpec.Literal -> host.host
                is HostSpec.Wildcard -> "*.${host.suffix}"
            }
        return if (authority == null) {
            filter
        } else {
            filter.addDataAuthority(authority, port)
            if (addPath(filter, spec.path)) filter else null
        }
    }

    /** Returns false when [path] needs a newer Android version than this device runs. */
    private fun addPath(
        filter: IntentFilter,
        path: PathSpec,
    ): Boolean {
        var isSupported = true
        when (path) {
            PathSpec.None -> Unit
            is PathSpec.Literal -> filter.addDataPath(path.value, PatternMatcher.PATTERN_LITERAL)
            is PathSpec.Prefix -> filter.addDataPath(path.value, PatternMatcher.PATTERN_PREFIX)
            is PathSpec.Pattern -> filter.addDataPath(path.value, PatternMatcher.PATTERN_SIMPLE_GLOB)
            is PathSpec.AdvancedPattern ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    filter.addDataPath(path.value, PatternMatcher.PATTERN_ADVANCED_GLOB)
                } else {
                    isSupported = false
                }
            is PathSpec.Suffix ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    filter.addDataPath(path.value, PatternMatcher.PATTERN_SUFFIX)
                } else {
                    isSupported = false
                }
        }
        return isSupported
    }
}
