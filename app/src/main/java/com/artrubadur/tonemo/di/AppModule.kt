package com.artrubadur.tonemo.di

import com.artrubadur.tonemo.data.model.ActiveModelStore
import com.artrubadur.tonemo.data.model.ModelMetadataRepository
import com.artrubadur.tonemo.data.model.ModelRepository
import com.artrubadur.tonemo.data.model.ModelService
import com.artrubadur.tonemo.runtime.llm.LiteRtLlmRuntime
import com.artrubadur.tonemo.runtime.llm.LlmRuntime
import com.artrubadur.tonemo.ui.screens.chat.DialogController
import org.koin.dsl.module

val appModule = module {
    single { ActiveModelStore(get()) }
    single { ModelRepository(get()) }
    single { ModelMetadataRepository(get()) }
    single { ModelService(get(), get()) }
    single<LlmRuntime> { LiteRtLlmRuntime(get(), get()) }
    single { DialogController(get()) }
}
