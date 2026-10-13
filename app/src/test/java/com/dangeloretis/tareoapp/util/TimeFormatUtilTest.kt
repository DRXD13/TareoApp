package com.dangeloretis.tareoapp.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatUtilTest {
    @Test
    fun `format hours and minutes`() {
        assertEquals("0 h 05 min", 5L.formatHoursMinutes())
        assertEquals("1 h 05 min", 65L.formatHoursMinutes())
        assertEquals("2 h 30 min", 150L.formatHoursMinutes())
        assertEquals("10 h 00 min", 600L.formatHoursMinutes())
    }
}
