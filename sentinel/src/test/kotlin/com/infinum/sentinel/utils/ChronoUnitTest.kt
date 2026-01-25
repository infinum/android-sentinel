package com.infinum.sentinel.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for ChronoUnit enum.
 * Tests all enum values are defined correctly.
 */
internal class ChronoUnitTest {
    @Test
    fun `ChronoUnit has all expected values`() {
        val expected =
            listOf(
                ChronoUnit.NANOS,
                ChronoUnit.MICROS,
                ChronoUnit.MILLIS,
                ChronoUnit.SECONDS,
                ChronoUnit.MINUTES,
                ChronoUnit.HOURS,
                ChronoUnit.HALF_DAYS,
                ChronoUnit.DAYS,
                ChronoUnit.WEEKS,
                ChronoUnit.MONTHS,
                ChronoUnit.YEARS,
                ChronoUnit.DECADES,
                ChronoUnit.CENTURIES,
                ChronoUnit.MILLENNIA,
                ChronoUnit.ERAS,
                ChronoUnit.FOREVER,
            )

        val actual = ChronoUnit.values().toList()

        assertEquals(expected, actual)
    }

    @Test
    fun `ChronoUnit values count is correct`() {
        val expected = 16

        val actual = ChronoUnit.values().size

        assertEquals(expected, actual)
    }

    @Test
    fun `ChronoUnit NANOS is first value`() {
        val expected = ChronoUnit.NANOS

        val actual = ChronoUnit.values()[0]

        assertEquals(expected, actual)
    }

    @Test
    fun `ChronoUnit FOREVER is last value`() {
        val expected = ChronoUnit.FOREVER

        val actual = ChronoUnit.values().last()

        assertEquals(expected, actual)
    }

    @Test
    fun `ChronoUnit valueOf works for all values`() {
        ChronoUnit.values().forEach { unit ->
            val result = ChronoUnit.valueOf(unit.name)
            assertEquals(unit, result)
        }
    }

    @Test
    fun `ChronoUnit name property is correct`() {
        assertEquals("NANOS", ChronoUnit.NANOS.name)
        assertEquals("SECONDS", ChronoUnit.SECONDS.name)
        assertEquals("DAYS", ChronoUnit.DAYS.name)
        assertEquals("FOREVER", ChronoUnit.FOREVER.name)
    }

    @Test
    fun `ChronoUnit ordinal values are sequential`() {
        val units = ChronoUnit.values()

        for (i in units.indices) {
            assertEquals(i, units[i].ordinal)
        }
    }
}
