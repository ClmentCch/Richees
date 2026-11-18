package com.richyss.app.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormat {

    /** 3725000 -> "1:02:05", 65000 -> "01:05". Negative values are clamped to 0. */
    fun duration(millis: Long): String {
        val total = (millis.coerceAtLeast(0)) / 1000
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val seconds = total % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /** "12/03 21:45" in the device locale. */
    fun shortDateTime(epochMillis: Long, locale: Locale = Locale.getDefault()): String {
        if (epochMillis <= 0) return "—"
        val format = SimpleDateFormat("dd/MM HH:mm", locale)
        return format.format(Date(epochMillis))
    }

    /** "Il y a 3 min" style relative label, without external dependencies. */
    fun relative(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
        if (epochMillis <= 0) return "—"
        val delta = now - epochMillis
        if (delta < 0) return shortDateTime(epochMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(delta)
        val hours = TimeUnit.MILLISECONDS.toHours(delta)
        val days = TimeUnit.MILLISECONDS.toDays(delta)
        return when {
            minutes < 1 -> "à l'instant"
            minutes < 60 -> "${minutes}min"
            hours < 24 -> "${hours}h"
            days < 30 -> "${days}j"
            else -> shortDateTime(epochMillis)
        }
    }

    /** Elapsed time label for the presence preview, e.g. "01:23 elapsed". */
    fun timerLabel(
        start: Long?,
        end: Long?,
        now: Long,
        elapsedSuffix: String,
        remainingSuffix: String
    ): String? {
        if (start != null && start > 0) {
            return "${duration(now - start)} $elapsedSuffix"
        }
        if (end != null && end > 0) {
            return "${duration(end - now)} $remainingSuffix"
        }
        return null
    }
}
