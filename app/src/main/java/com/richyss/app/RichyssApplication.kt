package com.richyss.app

import android.app.Application
import com.richyss.app.core.AppContainer

class RichyssApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
