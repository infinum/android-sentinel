package com.infinum.sentinel.ui.deeplinks.editor

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Checks [AndroidLinkSpecMatcher] against the platform's own matching with literal cases.
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
internal class AndroidLinkSpecMatcherTests {
    private val matcher = AndroidLinkSpecMatcher()

    @Test
    fun prefix_matchesOnlyPathsUnderThePrefix() {
        val spec = LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.Prefix("/orders/"))

        assertEquals(true, matcher.matches(spec, "https://example.com/orders/1"))
        assertEquals(false, matcher.matches(spec, "https://example.com/order/1"))
    }

    @Test
    fun wildcardHost_matchesSubdomainsButNotTheBareDomain() {
        val spec = LinkSpec("https", HostSpec.Wildcard(".example.com"), null, PathSpec.None)

        assertEquals(true, matcher.matches(spec, "https://shop.example.com"))
        assertEquals(false, matcher.matches(spec, "https://example.com"))
    }

    @Test
    fun simplePattern_matchesALinkThatFits() {
        val spec = LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.Pattern("/p/.*"))

        assertEquals(true, matcher.matches(spec, "https://example.com/p/x"))
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
    fun suffix_matchesOnlyPathsWithTheSuffix() {
        val spec = LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.Suffix(".pdf"))

        assertEquals(true, matcher.matches(spec, "https://example.com/a/b.pdf"))
        assertEquals(false, matcher.matches(spec, "https://example.com/a/b.txt"))
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.S)
    fun advancedPattern_matchesOnlyPathsThatFit() {
        val spec = LinkSpec("https", HostSpec.Literal("example.com"), null, PathSpec.AdvancedPattern("/a/[0-9]+"))

        assertEquals(true, matcher.matches(spec, "https://example.com/a/42"))
        assertEquals(false, matcher.matches(spec, "https://example.com/a/x"))
    }
}
