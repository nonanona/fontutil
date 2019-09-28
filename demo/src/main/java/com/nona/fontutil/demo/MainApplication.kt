package com.nona.fontutil.demo

import android.app.Application
import com.nona.fontutil.assets.CustomTagParserManager
import com.nona.fontutil.provider.FontProviderTagParser

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        CustomTagParserManager.register(
            "Google",
            FontProviderTagParser(this, "com.google.android.gms.fonts"))
    }
}