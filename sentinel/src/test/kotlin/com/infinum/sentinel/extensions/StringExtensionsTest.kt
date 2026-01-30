package com.infinum.sentinel.extensions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for String extension functions.
 * Tests cover sanitization, occurrence finding, and ASN parsing.
 */
internal class StringExtensionsTest {
    @Test
    fun `sanitize converts to lowercase`() {
        val input = "HelloWorld"
        val expected = "helloworld"

        val result = input.sanitize()

        assertEquals(expected, result)
    }

    @Test
    fun `sanitize replaces spaces with underscores`() {
        val input = "Hello World Test"
        val expected = "hello_world_test"

        val result = input.sanitize()

        assertEquals(expected, result)
    }

    @Test
    fun `sanitize handles mixed case and spaces`() {
        val input = "Test Case Example"
        val expected = "test_case_example"

        val result = input.sanitize()

        assertEquals(expected, result)
    }

    @Test
    fun `sanitize handles empty string`() {
        val input = ""
        val expected = ""

        val result = input.sanitize()

        assertEquals(expected, result)
    }

    @Test
    fun `sanitize handles already sanitized string`() {
        val input = "already_sanitized"
        val expected = "already_sanitized"

        val result = input.sanitize()

        assertEquals(expected, result)
    }

    @Test
    fun `allOccurrenceIndexes finds single occurrence`() {
        val input = "hello world"
        val term = "world"

        val result = input.allOccurrenceIndexes(term)

        assertEquals(1, result.size)
        assertEquals(6, result[0])
    }

    @Test
    fun `allOccurrenceIndexes finds multiple occurrences`() {
        val input = "test test test"
        val term = "test"

        val result = input.allOccurrenceIndexes(term)

        assertEquals(3, result.size)
        assertEquals(0, result[0])
        assertEquals(5, result[1])
        assertEquals(10, result[2])
    }

    @Test
    fun `allOccurrenceIndexes is case insensitive`() {
        val input = "Hello hello HELLO"
        val term = "hello"

        val result = input.allOccurrenceIndexes(term)

        assertEquals(3, result.size)
    }

    @Test
    fun `allOccurrenceIndexes returns empty for no matches`() {
        val input = "hello world"
        val term = "notfound"

        val result = input.allOccurrenceIndexes(term)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `allOccurrenceIndexes finds overlapping occurrences`() {
        val input = "aaa"
        val term = "aa"

        val result = input.allOccurrenceIndexes(term)

        assertEquals(2, result.size)
        assertEquals(0, result[0])
        assertEquals(1, result[1])
    }

    @Test
    fun `asASN parses single key-value pair`() {
        val input = "CN=example.com"

        val result = input.asASN()

        assertEquals(1, result.size)
        assertEquals("CN = example.com", result[0])
    }

    @Test
    fun `asASN parses multiple key-value pairs`() {
        val input = "CN=example.com,O=Company,C=US"

        val result = input.asASN()

        assertEquals(3, result.size)
        assertEquals("CN = example.com", result[0])
        assertEquals("O = Company", result[1])
        assertEquals("C = US", result[2])
    }

    @Test
    fun `asASN handles spaces around values`() {
        val input = "CN= example.com ,O= Company "

        val result = input.asASN()

        assertEquals(2, result.size)
        assertEquals("CN = example.com", result[0])
        assertEquals("O = Company", result[1])
    }

    @Test
    fun `asASN handles empty string`() {
        val input = ""

        val result = input.asASN()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `asASN handles complex certificate subject`() {
        val input = "CN=www.example.com,OU=IT Department,O=Example Corp,L=San Francisco,ST=California,C=US"

        val result = input.asASN()

        assertEquals(6, result.size)
        assertEquals("CN = www.example.com", result[0])
        assertEquals("OU = IT Department", result[1])
        assertEquals("O = Example Corp", result[2])
        assertEquals("L = San Francisco", result[3])
        assertEquals("ST = California", result[4])
        assertEquals("C = US", result[5])
    }

    @Test
    fun `asASN handles newline separated pairs`() {
        val input = "CN=example.com\nO=Company\nC=US"

        val result = input.asASN()

        assertEquals(3, result.size)
    }
}
