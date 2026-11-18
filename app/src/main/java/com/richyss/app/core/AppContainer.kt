package com.richyss.app.core

import android.content.Context
import com.richyss.app.data.database.RichyssDatabase
import com.richyss.app.data.prefs.SettingsRepository
import com.richyss.app.data.repository.PresetRepository
import com.richyss.app.data.repository.ProfileRepository
import com.richyss.app.network.GitHubClient
import com.richyss.app.network.HttpClient
import com.richyss.app.network.UpdateChecker
import com.richyss.app.rpc.DiscordBridge
import com.richyss.app.rpc.DiscordIntegrationService

/**
 * Manual dependency container: small enough that a DI framework would add
 * indirection without removing any work.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val settings = SettingsRepository(appContext)

    private val database = RichyssDatabase.build(appContext)

    val profiles = ProfileRepository(database)

    private val http = HttpClient()

    val gitHub = GitHubClient(http)

    val presets = PresetRepository(appContext, gitHub, settings)

    val discord = DiscordIntegrationService(DiscordBridge(appContext))

    val updater = UpdateChecker(gitHub)

    val messages = MessageBus()
}
