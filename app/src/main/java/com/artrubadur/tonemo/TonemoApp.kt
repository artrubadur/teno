package com.artrubadur.tonemo

import android.app.Application
import com.artrubadur.tonemo.di.agentModule
import com.artrubadur.tonemo.di.appModule
import com.artrubadur.tonemo.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TonemoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TonemoApp)
            modules(appModule, agentModule, databaseModule)
        }
    }
}
