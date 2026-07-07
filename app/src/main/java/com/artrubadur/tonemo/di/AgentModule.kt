package com.artrubadur.tonemo.di

import com.artrubadur.tonemo.agent.orchestration.AgentOrchestrator
import com.artrubadur.tonemo.agent.policy.ConfirmationManager
import com.artrubadur.tonemo.agent.policy.SafetyPolicy
import com.artrubadur.tonemo.agent.tools.Tool
import com.artrubadur.tonemo.agent.tools.ToolBroker
import com.artrubadur.tonemo.agent.tools.ToolRegistry
import com.artrubadur.tonemo.agent.tools.impl.GetCurrentTimeTool
import com.artrubadur.tonemo.agent.tools.impl.LogTool
import org.koin.dsl.bind
import org.koin.dsl.module

val agentModule = module {
    factory { LogTool() } bind Tool::class
    factory { GetCurrentTimeTool() } bind Tool::class

    single {
        ToolRegistry(
            tools = getAll<Tool<*>>()
        )
    }

    single { ConfirmationManager() }
    single { SafetyPolicy() }
    single { ToolBroker(get(), get()) }
    single { AgentOrchestrator(get(), get(), get()) }
}
