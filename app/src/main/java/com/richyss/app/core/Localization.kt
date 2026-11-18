package com.richyss.app.core

import androidx.compose.runtime.staticCompositionLocalOf
import com.richyss.app.data.prefs.LanguageMode
import java.util.Locale

/** Current in-app translation, provided once at the root of the composition. */
val LocalTranslations = staticCompositionLocalOf<Translations> { French }

/** Maps the stored language preference to a catalogue. */
fun resolveTranslations(mode: LanguageMode): Translations = when (mode) {
    LanguageMode.FRENCH -> French
    LanguageMode.ENGLISH -> English
    LanguageMode.SYSTEM -> {
        val tag = Locale.getDefault().language
        if (tag.equals("en", ignoreCase = true)) English else French
    }
}
