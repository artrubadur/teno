package com.artrubadur.teno

import android.app.Application
import com.artrubadur.teno.di.agentModule
import com.artrubadur.teno.di.appModule
import com.artrubadur.teno.di.connectionModule
import com.artrubadur.teno.di.databaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TenoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TenoApp)
            modules(appModule, agentModule, databaseModule, connectionModule)
        }
    }
}
