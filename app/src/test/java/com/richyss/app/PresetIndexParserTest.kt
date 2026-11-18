package com.richyss.app

import com.richyss.app.data.models.PresetCategory
import com.richyss.app.data.models.PresetIndexParser
import com.richyss.app.data.models.PresetIndexResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PresetIndexParserTest {

    private fun index(vararg entries: String): String = """
        {
          "schema": 1,
          "version": "1.0.0",
          "presets": [ ${entries.joinToString(",")} ]
        }
    """.trimIndent()

    private fun entry(id: String, path: String) = """
        {"id": "$id", "name": "$id", "description": "d", "category": "gaming",
         "author": "Richyss", "version": "1.0.0", "path": "$path"}
    """.trimIndent()

    @Test
    fun parsesValidIndex() {
        val result = PresetIndexParser.parse(
            index(entry("gaming-one", "presets/gaming-one.json"))
        )
        assertTrue(result is PresetIndexResult.Success)
        val presets = (result as PresetIndexResult.Success).index.presets
        assertEquals(1, presets.size)
        assertEquals("gaming-one", presets.first().id)
    }

    @Test
    fun dropsPathTraversalEntries() {
        val result = PresetIndexParser.parse(
            index(
                entry("ok", "presets/ok.json"),
                entry("escape", "presets/../../secrets.json"),
                entry("absolute", "/etc/passwd"),
                entry("outside", "other/ok.json")
            )
        )
        val presets = (result as PresetIndexResult.Success).index.presets
        assertEquals(1, presets.size)
        assertEquals("ok", presets.first().id)
    }

    @Test
    fun dropsDuplicateIds() {
        val result = PresetIndexParser.parse(
            index(
                entry("dup", "presets/a.json"),
                entry("dup", "presets/b.json")
            )
        )
        val presets = (result as PresetIndexResult.Success).index.presets
        assertEquals(1, presets.size)
    }

    @Test
    fun dropsNonJsonEntries() {
        val result = PresetIndexParser.parse(index(entry("binary", "presets/binary.dex")))
        assertTrue((result as PresetIndexResult.Success).index.presets.isEmpty())
    }

    @Test
    fun reportsInvalidJson() {
        val result = PresetIndexParser.parse("<html>not json</html>")
        assertTrue(result is PresetIndexResult.Failure)
    }

    @Test
    fun categoryFallsBackToCustom() {
        assertEquals(PresetCategory.CUSTOM, PresetCategory.fromKey("unknown"))
        assertEquals(PresetCategory.MUSIC, PresetCategory.fromKey("MUSIC"))
        assertEquals(PresetCategory.CUSTOM, PresetCategory.fromKey(null))
        assertEquals(PresetCategory.PROGRAMMING, PresetCategory.fromKey("programming"))
    }
}
