package com.richyss.app

import com.richyss.app.data.models.ActivityType
import com.richyss.app.data.models.ProfileButton
import com.richyss.app.data.models.ProfileJson
import com.richyss.app.data.models.ProfileLimits
import com.richyss.app.data.models.ProfileValidator
import com.richyss.app.data.models.RpcProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ProfileJsonTest {

    private fun profile() = RpcProfile(
        id = "profile-1",
        name = "Richyss demo",
        icon = "🟣",
        description = "Test profile",
        activityType = ActivityType.LISTENING.id,
        activityName = "Richyss",
        details = "Editing a profile",
        state = "Live preview",
        largeImage = "content://media/1",
        smallImage = "",
        largeText = "Richyss",
        smallText = "",
        timerEnabled = true,
        startTimestamp = 1_700_000_000_000,
        buttons = listOf(ProfileButton("Repo", "https://github.com/ClmentCch/Richees"))
    )

    @Test
    fun roundTripPreservesEveryField() {
        val original = profile()
        val decoded = ProfileJson.decode(ProfileJson.encode(original)).getOrThrow()

        assertEquals(original, decoded)
    }

    @Test
    fun decodeIgnoresUnknownFieldsFromNewerTools() {
        val json = """
            {
              "schema": 1,
              "id": "abc",
              "name": "Imported",
              "futureField": {"nested": true}
            }
        """.trimIndent()

        val decoded = ProfileJson.decode(json).getOrThrow()
        assertEquals("abc", decoded.id)
        assertEquals("Imported", decoded.name)
        assertEquals(ActivityType.PLAYING.id, decoded.activityType)
    }

    @Test
    fun decodeRejectsNewerSchema() {
        val json = """{"schema": 99, "id": "abc", "name": "x"}"""
        assertTrue(ProfileJson.decode(json).isFailure)
    }

    @Test
    fun decodeRejectsMissingId() {
        val json = """{"schema": 1, "name": "x", "id": ""}"""
        assertTrue(ProfileJson.decode(json).isFailure)
    }

    @Test
    fun decodeRejectsBrokenJson() {
        assertTrue(ProfileJson.decode("{ nope }").isFailure)
    }

    @Test
    fun payloadOnlyContainsBridgeFields() {
        val payload = ProfileJson.encodePayload(profile(), now = 1_700_000_100_000)
        val root = Json.parseToJsonElement(payload).jsonObject

        assertFalse(root.containsKey("id"))
        assertFalse(root.containsKey("description"))
        assertFalse(root.containsKey("activityType"))
        assertEquals("Richyss", root["activityName"]!!.jsonPrimitive.content)
        val buttons = root["buttons"]!!.jsonArray
        assertEquals(1, buttons.size)
        assertEquals("Repo", buttons[0].jsonObject["label"]!!.jsonPrimitive.content)
        assertEquals(
            "https://github.com/ClmentCch/Richees",
            buttons[0].jsonObject["url"]!!.jsonPrimitive.content
        )
    }

    @Test
    fun payloadUsesNowWhenTimerHasNoStart() {
        val payload = ProfileJson.encodePayload(
            profile().copy(startTimestamp = null, endTimestamp = null),
            now = 42_000
        )
        val root = Json.parseToJsonElement(payload).jsonObject
        assertEquals("42000", root["startTimestamp"]!!.jsonPrimitive.content)
    }

    @Test
    fun payloadDropsButtonsWhenTimerDisabled() {
        val payload = ProfileJson.encodePayload(
            profile().copy(timerEnabled = false, startTimestamp = null, endTimestamp = null),
            now = 42_000
        )
        val root = Json.parseToJsonElement(payload).jsonObject
        assertFalse(root.containsKey("startTimestamp"))
    }

    @Test
    fun validProfileHasNoIssues() {
        assertTrue(ProfileValidator.validate(profile()).isEmpty())
    }

    @Test
    fun validatorRequiresNameAndActivityName() {
        val issues = ProfileValidator.validate(profile().copy(name = " ", activityName = ""))
        assertTrue(issues.any { it.field == "name" && it.messageKey == "required" })
        assertTrue(issues.any { it.field == "activityName" && it.messageKey == "required" })
    }

    @Test
    fun validatorRejectsInsecureButtonUrl() {
        val issues = ProfileValidator.validate(
            profile().copy(buttons = listOf(ProfileButton("x", "javascript:alert(1)")))
        )
        assertTrue(issues.any { it.messageKey == "invalid_url" })
    }

    @Test
    fun validatorRejectsMoreThanTwoButtons() {
        val issues = ProfileValidator.validate(
            profile().copy(
                buttons = listOf(
                    ProfileButton("a", "https://a.example"),
                    ProfileButton("b", "https://b.example"),
                    ProfileButton("c", "https://c.example")
                )
            )
        )
        assertTrue(issues.any { it.messageKey == "too_many" })
    }

    @Test
    fun validatorRejectsReversedTimestamps() {
        val issues = ProfileValidator.validate(
            profile().copy(startTimestamp = 2_000, endTimestamp = 1_000)
        )
        assertTrue(issues.any { it.messageKey == "before_start" })
    }

    @Test
    fun validatorRejectsOversizedPayload() {
        // Emoji are four bytes in UTF-8, so a field at its character limit can push
        // the serialised payload past the bridge's 4096-byte ceiling.
        val wide = "🟣".repeat(ProfileLimits.DETAILS)
        val wideImage = "🟣".repeat(ProfileLimits.LARGE_IMAGE)
        val issues = ProfileValidator.validate(
            profile().copy(
                activityName = "🟣".repeat(ProfileLimits.ACTIVITY_NAME),
                details = wide,
                state = wide,
                largeImage = wideImage,
                smallImage = wideImage,
                largeText = wide,
                smallText = wide,
                buttons = listOf(
                    ProfileButton("🟣".repeat(32), "https://example.com/" + "d".repeat(400)),
                    ProfileButton("🟣".repeat(32), "https://example.com/" + "e".repeat(400))
                )
            )
        )
        assertTrue(issues.any { it.messageKey == "too_large" })
    }

    @Test
    fun timerWithoutTimesIsRejected() {
        val issues = ProfileValidator.validate(
            profile().copy(startTimestamp = null, endTimestamp = null, timerEnabled = true)
        )
        assertTrue(issues.any { it.messageKey == "no_times" })
    }
}
