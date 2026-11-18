package com.richyss.app.core

import com.richyss.app.data.prefs.LanguageMode
import kotlinx.coroutines.flow.first

/**
 * View models emit already-translated messages, so they read the stored language
 * preference instead of holding a composition local.
 */
suspend fun translationFor(container: AppContainer): Translations =
    resolveTranslations(container.settings.settings.first().language)

/** Non-suspending variant used where the mode is already known. */
fun translationFor(mode: LanguageMode): Translations = resolveTranslations(mode)
