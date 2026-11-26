package com.infinum.sentinel.domain.deeplinks.verification

import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [VerificationStatusResolver].
 */
internal class VerificationStatusResolverTest {
    private fun snapshot(vararg states: Pair<String, Int>) =
        DomainVerificationData(isLinkHandlingAllowed = true, hostStates = mapOf(*states))

    @Test
    fun `exact host maps state 2 to verified`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("https", "example.com", snapshot("example.com" to 2)),
        )
    }

    @Test
    fun `exact host maps state 1 to enabled in settings`() {
        assertEquals(
            VerificationStatus.ENABLED_IN_SETTINGS,
            VerificationStatusResolver.resolve("https", "example.com", snapshot("example.com" to 1)),
        )
    }

    @Test
    fun `exact host maps state 0 to not verified`() {
        assertEquals(
            VerificationStatus.NOT_VERIFIED,
            VerificationStatusResolver.resolve("https", "example.com", snapshot("example.com" to 0)),
        )
    }

    @Test
    fun `undeclared host is not registered`() {
        assertEquals(
            VerificationStatus.NOT_REGISTERED,
            VerificationStatusResolver.resolve("https", "example.org", snapshot()),
        )
    }

    @Test
    fun `wildcard key matches a subdomain`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("https", "shop.example.com", snapshot("*.example.com" to 2)),
        )
    }

    @Test
    fun `wildcard key does not match the bare domain`() {
        assertEquals(
            VerificationStatus.NOT_REGISTERED,
            VerificationStatusResolver.resolve("https", "example.com", snapshot("*.example.com" to 2)),
        )
    }

    @Test
    fun `unfilled wildcard template host matches its own key`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("https", "*.example.com", snapshot("*.example.com" to 2)),
        )
    }

    @Test
    fun `the best state among matching wildcard keys wins`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve(
                "https",
                "a.shop.example.com",
                snapshot("*.example.com" to 0, "*.shop.example.com" to 2),
            ),
        )
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve(
                "https",
                "a.shop.example.com",
                snapshot("*.example.com" to 2, "*.shop.example.com" to 0),
            ),
        )
    }

    @Test
    fun `a verified wildcard key wins over an unverified exact key`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("https", "shop.example.com", snapshot("shop.example.com" to 0, "*.example.com" to 2)),
        )
    }

    @Test
    fun `a verified wildcard key approves the declared bare domain`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("https", "example.com", snapshot("example.com" to 0, "*.example.com" to 2)),
        )
    }

    @Test
    fun `a selected exact key wins over an unverified wildcard key`() {
        assertEquals(
            VerificationStatus.ENABLED_IN_SETTINGS,
            VerificationStatusResolver.resolve("https", "shop.example.com", snapshot("shop.example.com" to 1, "*.example.com" to 0)),
        )
    }

    @Test
    fun `verified wins over selected whichever key holds it`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("https", "shop.example.com", snapshot("shop.example.com" to 1, "*.example.com" to 2)),
        )
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("https", "shop.example.com", snapshot("shop.example.com" to 2, "*.example.com" to 1)),
        )
    }

    @Test
    fun `a selected wildcard key wins over an unverified exact key`() {
        assertEquals(
            VerificationStatus.ENABLED_IN_SETTINGS,
            VerificationStatusResolver.resolve("https", "shop.example.com", snapshot("shop.example.com" to 0, "*.example.com" to 1)),
        )
    }

    @Test
    fun `a subdomain only a wildcard key covers is declared`() {
        assertEquals(
            VerificationStatus.NOT_VERIFIED,
            VerificationStatusResolver.resolve("https", "shop.example.com", snapshot("*.example.com" to 0)),
        )
    }

    @Test
    fun `host is matched case-insensitively`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("https", "EXAMPLE.com", snapshot("example.com" to 2)),
        )
    }

    @Test
    fun `snapshot keys are matched case-insensitively`() {
        assertEquals(
            VerificationStatus.ENABLED_IN_SETTINGS,
            VerificationStatusResolver.resolve("https", "example.com", snapshot("Example.COM" to 1)),
        )
    }

    @Test
    fun `unknown state value maps to not verified`() {
        assertEquals(
            VerificationStatus.NOT_VERIFIED,
            VerificationStatusResolver.resolve("https", "example.com", snapshot("example.com" to 7)),
        )
    }

    @Test
    fun `custom scheme returns null`() {
        assertNull(VerificationStatusResolver.resolve("myapp", "example.com", snapshot("example.com" to 2)))
    }

    @Test
    fun `scheme is matched case-insensitively`() {
        assertEquals(
            VerificationStatus.VERIFIED,
            VerificationStatusResolver.resolve("HTTP", "example.com", snapshot("example.com" to 2)),
        )
    }

    @Test
    fun `null host returns null`() {
        assertNull(VerificationStatusResolver.resolve("https", null, snapshot("example.com" to 2)))
    }

    @Test
    fun `null snapshot returns null`() {
        assertNull(VerificationStatusResolver.resolve("https", "example.com", null))
    }
}
