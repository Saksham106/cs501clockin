package com.example.cs501clockin.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SessionDurationTest {

    @Test
    fun durationMillis_coercesNegativeSpanToZero() {
        val session = Session(
            id = 1L,
            tag = "Work",
            startTimeMillis = 100,
            endTimeMillis = 50
        )
        assertEquals(0L, session.durationMillis())
    }

    @Test
    fun durationMillis_activeUsesProvidedNow() {
        val session = Session(
            id = 1L,
            tag = "School",
            startTimeMillis = 1000L,
            endTimeMillis = null
        )
        assertEquals(500L, session.durationMillis(nowMillis = 1500L))
    }
}
