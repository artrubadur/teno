package com.artrubadur.tonemo.di

import com.artrubadur.tonemo.agent.orchestration.AgentOrchestrator
import com.artrubadur.tonemo.agent.orchestration.AgentPromptBuilder
import com.artrubadur.tonemo.agent.policy.ConfirmationManager
import com.artrubadur.tonemo.agent.policy.SafetyPolicy
import com.artrubadur.tonemo.agent.tools.AgentTool
import com.artrubadur.tonemo.agent.tools.ToolBroker
import com.artrubadur.tonemo.agent.tools.ToolRegistry
import com.artrubadur.tonemo.agent.tools.impl.LogTool
import com.artrubadur.tonemo.agent.tools.impl.TimeTool
import org.koin.dsl.bind
import org.koin.dsl.module

val agentModule = module {
    factory { LogTool() } bind AgentTool::class
    factory { TimeTool() } bind AgentTool::class

    single {
        ToolRegistry(
            tools = getAll<AgentTool<*>>()
        )
    }

    single { ConfirmationManager() }
    single { SafetyPolicy() }
    single { AgentPromptBuilder() }
    single { ToolBroker(get(), get()) }
    single { AgentOrchestrator(get(), get(), get(), get()) }
}
