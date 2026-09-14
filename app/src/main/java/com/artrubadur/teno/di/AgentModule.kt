package com.artrubadur.teno.di

import com.artrubadur.teno.agent.orchestration.AgentOrchestrator
import com.artrubadur.teno.agent.policy.ConfirmationManager
import com.artrubadur.teno.agent.policy.SafetyPolicy
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolBroker
import com.artrubadur.teno.agent.tools.ToolManager
import com.artrubadur.teno.agent.tools.ToolRegistry
import com.artrubadur.teno.agent.tools.impl.ClickScreenNodeTool
import com.artrubadur.teno.agent.tools.impl.GetBrightnessTool
import com.artrubadur.teno.agent.tools.impl.GetClipboardTool
import com.artrubadur.teno.agent.tools.impl.GetCurrentTimeTool
import com.artrubadur.teno.agent.tools.impl.GetScreenNodeTool
import com.artrubadur.teno.agent.tools.impl.GetScreenTreeTool
import com.artrubadur.teno.agent.tools.impl.GetVolumeTool
import com.artrubadur.teno.agent.tools.impl.LogTool
import com.artrubadur.teno.agent.tools.impl.SetBrightnessTool
import com.artrubadur.teno.agent.tools.impl.SetClipboardTool
import com.artrubadur.teno.agent.tools.impl.SetVolumeTool
import com.artrubadur.teno.agent.tools.impl.ToggleFlashlightTool
import com.artrubadur.teno.agent.tools.integrations.ScreenNodeClicker
import com.artrubadur.teno.agent.tools.integrations.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.ScreenTreeReader
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val agentModule = module {
    single { ScreenNodeStore() }
    single { ScreenTreeReader(androidContext()) }
    single { ScreenNodeClicker(get()) }

    factory { LogTool() } bind Tool::class
    factory { GetCurrentTimeTool() } bind Tool::class
    factory { GetBrightnessTool(androidContext()) } bind Tool::class
    factory { SetBrightnessTool(androidContext()) } bind Tool::class
    factory { GetVolumeTool(androidContext()) } bind Tool::class
    factory { SetVolumeTool(androidContext()) } bind Tool::class
    factory { ToggleFlashlightTool(androidContext()) } bind Tool::class
    factory { GetClipboardTool(androidContext()) } bind Tool::class
    factory { SetClipboardTool(androidContext()) } bind Tool::class
    factory { GetScreenTreeTool(get(), get()) } bind Tool::class
    factory { GetScreenNodeTool(get(), get()) } bind Tool::class
    factory { ClickScreenNodeTool(get(), get()) } bind Tool::class

    single {
        ToolRegistry(
            tools = getAll<Tool<*>>()
        )
    }
    single { ToolManager(androidContext(), get(), get()) }

    single { ConfirmationManager() }
    single { SafetyPolicy() }
    single { ToolBroker(get(), get(), get()) }
    single { AgentOrchestrator(get(), get(), get(), get(), get()) }
}

