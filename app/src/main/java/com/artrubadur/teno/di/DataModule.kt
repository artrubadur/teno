package com.artrubadur.teno.di

import androidx.room.Room
import com.artrubadur.teno.connection.ConnectionManager
import com.artrubadur.teno.data.connection.ConnectionStore
import com.artrubadur.teno.data.database.AppDatabase
import com.artrubadur.teno.data.model.ModelRepository
import com.artrubadur.teno.data.model.ModelStore
import com.artrubadur.teno.data.tools.ToolSettingsStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "teno_database"
        ).enableMultiInstanceInvalidation().build()
    }
    single {
        get<AppDatabase>().connectionDao()
    }

    single { ConnectionStore(get()) }

    single { ModelRepository(get()) }
    single { ModelStore(get(), androidContext()) }

    single { ConnectionManager(get(), get()) }
    single { ToolSettingsStore(androidContext()) }
}
