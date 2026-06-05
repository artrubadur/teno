package com.artrubadur.tonemo.di

import com.artrubadur.tonemo.data.model.ModelMetadataRepository
import com.artrubadur.tonemo.data.model.ModelRepository
import com.artrubadur.tonemo.data.model.ModelService
import org.koin.dsl.module

val appModule = module {
    single { ModelRepository(get()) }
    single { ModelMetadataRepository(get()) }
    single { ModelService(get(), get()) }
}
