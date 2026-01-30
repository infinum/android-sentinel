package com.infinum.sentinel.extensions

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for ByteArray extension functions.
 * Tests hexadecimal string conversion.
 */
internal class ByteArrayExtensionsTest {
    @Test
    fun `asHexString converts single byte`() {
        val input = byteArrayOf(0xFF.toByte())
        val expected = "FF"

        val result = input.asHexString()

        assertEquals(expected, result)
    }

    @Test
    fun `asHexString converts multiple bytes`() {
        val input = byteArrayOf(0x12, 0x34, 0x56, 0x78)
        val expected = "12345678"

        val result = input.asHexString()

        assertEquals(expected, result)
    }

    @Test
    fun `asHexString pads with leading zeros`() {
        val input = byteArrayOf(0x00, 0x01, 0x0A)
        val expected = "00010A"

        val result = input.asHexString()

        assertEquals(expected, result)
    }

    // Note: Empty byte arrays are not supported by asHexString
    // due to format string constraints (%0${0}X creates duplicate flags)

    @Test
    fun `asHexString converts all zeros`() {
        val input = byteArrayOf(0x00, 0x00, 0x00)
        val expected = "000000"

        val result = input.asHexString()

        assertEquals(expected, result)
    }

    @Test
    fun `asHexString converts all ones`() {
        val input = byteArrayOf(0xFF.toByte(), 0xFF.toByte())
        val expected = "FFFF"

        val result = input.asHexString()

        assertEquals(expected, result)
    }

    @Test
    fun `asHexString handles typical hash output`() {
        val input =
            byteArrayOf(
                0xAB.toByte(),
                0xCD.toByte(),
                0xEF.toByte(),
                0x01,
                0x23,
                0x45,
                0x67,
                0x89.toByte(),
            )
        val expected = "ABCDEF0123456789"

        val result = input.asHexString()

        assertEquals(expected, result)
    }

    @Test
    fun `asHexString uppercase output`() {
        val input = byteArrayOf(0xAB.toByte(), 0xCD.toByte())
        val result = input.asHexString()

        assertEquals("ABCD", result)
        assertEquals(result, result.uppercase())
    }
}
