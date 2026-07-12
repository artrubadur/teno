package com.artrubadur.teno.di

import com.artrubadur.teno.agent.orchestration.AgentOrchestrator
import com.artrubadur.teno.agent.policy.ConfirmationManager
import com.artrubadur.teno.agent.policy.SafetyPolicy
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolBroker
import com.artrubadur.teno.agent.tools.ToolManager
import com.artrubadur.teno.agent.tools.ToolRegistry
import com.artrubadur.teno.agent.tools.impl.GetBrightnessTool
import com.artrubadur.teno.agent.tools.impl.GetCurrentTimeTool
import com.artrubadur.teno.agent.tools.impl.LogTool
import com.artrubadur.teno.agent.tools.impl.SetBrightnessTool
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val agentModule = module {
    factory { LogTool() } bind Tool::class
    factory { GetCurrentTimeTool() } bind Tool::class
    factory { GetBrightnessTool(androidContext()) } bind Tool::class
    factory { SetBrightnessTool(androidContext()) } bind Tool::class

    single {
        ToolRegistry(
            tools = getAll<Tool<*>>()
        )
    }
    single { ToolManager(androidContext(), get(), get()) }

    single { ConfirmationManager() }
    single { SafetyPolicy() }
    single { ToolBroker(get(), get(), get()) }
    single { AgentOrchestrator(get(), get(), get()) }
}

