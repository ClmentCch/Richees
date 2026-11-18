package com.richyss.app

import com.richyss.app.core.TimeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimeFormatTest {

    @Test
    fun formatsMinutesAndSeconds() {
        assertEquals("01:05", TimeFormat.duration(65_000))
        assertEquals("00:00", TimeFormat.duration(0))
    }

    @Test
    fun formatsHours() {
        assertEquals("1:02:05", TimeFormat.duration(3_725_000))
    }

    @Test
    fun clampsNegativeDurations() {
        assertEquals("00:00", TimeFormat.duration(-5_000))
    }

    @Test
    fun buildsElapsedLabel() {
        val label = TimeFormat.timerLabel(
            start = 1_000_000,
            end = null,
            now = 1_065_000,
            elapsedSuffix = "écoulé",
            remainingSuffix = "restant"
        )
        assertEquals("01:05 écoulé", label)
    }

    @Test
    fun buildsRemainingLabel() {
        val label = TimeFormat.timerLabel(
            start = null,
            end = 2_000_000,
            now = 1_940_000,
            elapsedSuffix = "écoulé",
            remainingSuffix = "restant"
        )
        assertEquals("01:00 restant", label)
    }

    @Test
    fun returnsNullWithoutTimes() {
        assertNull(TimeFormat.timerLabel(null, null, 0, "e", "r"))
    }

    @Test
    fun relativeLabels() {
        val now = 10_000_000L
        assertEquals("5min", TimeFormat.relative(now - 5 * 60_000, now))
        assertEquals("2h", TimeFormat.relative(now - 2 * 3_600_000, now))
        assertEquals("à l'instant", TimeFormat.relative(now - 1_000, now))
    }
}
