package com.artrubadur.tonemo.di

import androidx.room.Room
import com.artrubadur.tonemo.connection.ConnectionManager
import com.artrubadur.tonemo.data.connection.ConnectionStore
import com.artrubadur.tonemo.data.database.AppDatabase
import com.artrubadur.tonemo.data.model.ModelRepository
import com.artrubadur.tonemo.data.model.ModelStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "tonemo_database"
        ).build()
    }
    single {
        get<AppDatabase>().connectionDao()
    }

    single { ConnectionStore(get()) }

    single { ModelRepository(get()) }
    single { ModelStore(get(), androidContext()) }

    single { ConnectionManager(get(), get()) }
}