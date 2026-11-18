package com.richyss.app

import com.richyss.app.network.GitHubClient
import com.richyss.app.network.HttpClient
import com.richyss.app.network.UpdateChecker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    private val checker = UpdateChecker(GitHubClient(HttpClient()))

    @Test
    fun detectsNewerMinorVersion() {
        assertTrue(checker.isNewer("v1.1.0", "1.0.0"))
    }

    @Test
    fun detectsNewerPatchVersion() {
        assertTrue(checker.isNewer("richyss-apk-v1.0.1", "1.0.0"))
    }

    @Test
    fun sameVersionIsNotNewer() {
        assertFalse(checker.isNewer("v1.0.0", "1.0.0"))
    }

    @Test
    fun olderVersionIsNotNewer() {
        assertFalse(checker.isNewer("v0.9.9", "1.0.0"))
    }

    @Test
    fun ignoresPrereleaseSuffix() {
        assertFalse(checker.isNewer("v1.0.0-beta.2", "1.0.0"))
        assertTrue(checker.isNewer("v1.0.1-beta.2", "1.0.0"))
    }

    @Test
    fun rejectsMalformedVersions() {
        assertFalse(checker.isNewer("latest", "1.0.0"))
    }
}
