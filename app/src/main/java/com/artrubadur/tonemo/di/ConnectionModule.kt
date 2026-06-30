package com.artrubadur.tonemo.di

import com.artrubadur.tonemo.connection.ModelType
import com.artrubadur.tonemo.connection.runtime.RoutingLlmRuntime
import com.artrubadur.tonemo.connection.runtime.llm.LiteRtLlmRuntime
import com.artrubadur.tonemo.connection.runtime.llm.LlmRuntime
import com.artrubadur.tonemo.connection.runtime.llm.RemoteLlmRuntime
import org.koin.dsl.module

val connectionModule = module {
    single { RemoteLlmRuntime() }

    single {
        LiteRtLlmRuntime(get(), get())
    }

    single<LlmRuntime> {
        RoutingLlmRuntime(
            remoteRuntime = get(),
            localRuntimes = mapOf(
                ModelType.LITERTLM to get<LiteRtLlmRuntime>()
            )
        )
    }
}
