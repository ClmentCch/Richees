package com.richyss.app

import com.richyss.app.core.English
import com.richyss.app.core.French
import com.richyss.app.core.TranslationsCatalog
import com.richyss.app.core.resolveTranslations
import com.richyss.app.data.prefs.LanguageMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationsTest {

    @Test
    fun cataloguesAreDistinct() {
        assertEquals("fr", French.languageCode)
        assertEquals("en", English.languageCode)
        assertNotEquals(French.navHome, English.navHome)
    }

    @Test
    fun everyStringIsNonBlank() {
        // The interface already forces both catalogues to define every key; this
        // guards against an empty value slipping in.
        val frenchValues = French::class.java.declaredMethods
            .filter { method -> method.returnType == String::class.java }
            .mapNotNull { method -> runCatching { method.invoke(French) as String }.getOrNull() }
        val englishValues = English::class.java.declaredMethods
            .filter { method -> method.returnType == String::class.java }
            .mapNotNull { method -> runCatching { method.invoke(English) as String }.getOrNull() }

        assertTrue(frenchValues.isNotEmpty())
        assertTrue(frenchValues.none { it.isBlank() })
        assertTrue(englishValues.none { it.isBlank() })
    }

    @Test
    fun resolvesExplicitLanguages() {
        assertEquals(French, resolveTranslations(LanguageMode.FRENCH))
        assertEquals(English, resolveTranslations(LanguageMode.ENGLISH))
    }

    @Test
    fun allContainsBothCatalogues() {
        assertEquals(2, TranslationsCatalog.all.size)
    }
}
