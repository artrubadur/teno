package com.artrubadur.teno.di

import com.artrubadur.teno.connection.ModelType
import com.artrubadur.teno.connection.runtime.llm.LlmRuntime
import com.artrubadur.teno.connection.runtime.llm.RoutingLlmRuntime
import com.artrubadur.teno.connection.runtime.llm.local.LiteRtLlmRuntime
import com.artrubadur.teno.connection.runtime.llm.remote.RemoteLlmRuntime
import org.koin.dsl.module

val connectionModule = module {
    single { RemoteLlmRuntime() }

    single {
        LiteRtLlmRuntime(
            appContext = get(),
            modelStore = get()
        )
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
