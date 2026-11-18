package com.richyss.app

import com.richyss.app.rpc.BridgeJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BridgeJsonTest {

    @Test
    fun parsesBridgeStatus() {
        val status = BridgeJson.parseStatus(
            """
            {"bridge":true,"version":"1.0.0","protocol":"1",
             "discordVersion":"270.12","hasPresence":true,"updatedAt":1700000000000}
            """.trimIndent()
        ).getOrThrow()

        assertEquals("1.0.0", status.bridgeVersion)
        assertEquals("1", status.protocol)
        assertEquals("270.12", status.discordVersion)
        assertTrue(status.hasPresence)
        assertEquals(1_700_000_000_000, status.updatedAt)
    }

    @Test
    fun toleratesMissingFields() {
        val status = BridgeJson.parseStatus("""{"bridge":true}""").getOrThrow()
        assertEquals("", status.bridgeVersion)
        assertFalse(status.hasPresence)
    }

    @Test
    fun rejectsInvalidJson() {
        assertTrue(BridgeJson.parseStatus("null").isFailure)
        assertTrue(BridgeJson.parseStatus("[]").isFailure)
    }

    @Test
    fun protocolCompatibility() {
        assertTrue(BridgeJson.protocolIsSupported("1"))
        assertTrue(BridgeJson.protocolIsSupported(" 1 "))
        assertFalse(BridgeJson.protocolIsSupported("2"))
        assertFalse(BridgeJson.protocolIsSupported(""))
    }
}
