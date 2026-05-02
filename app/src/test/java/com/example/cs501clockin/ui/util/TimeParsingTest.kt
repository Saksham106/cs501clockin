package com.example.cs501clockin.ui.util

import org.junit.Assert.assertNull
import org.junit.Test

class TimeParsingTest {

    @Test
    fun parseSameDayTimeToEpochMillis_invalid_returnsNull() {
        assertNull(parseSameDayTimeToEpochMillis(0L, "not-a-time"))
        assertNull(parseSameDayTimeToEpochMillis(0L, ""))
    }
}
