package com.artrubadur.teno.di

import com.artrubadur.teno.agent.orchestration.AgentOrchestrator
import com.artrubadur.teno.agent.policy.ConfirmationManager
import com.artrubadur.teno.agent.policy.SafetyPolicy
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolBroker
import com.artrubadur.teno.agent.tools.ToolManager
import com.artrubadur.teno.agent.tools.ToolRegistry
import com.artrubadur.teno.agent.tools.impl.device.DeviceGetBrightnessTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceGetVolumeTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceSetBrightnessTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceSetVolumeTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceToggleFlashlightTool
import com.artrubadur.teno.agent.tools.impl.diagnostics.DiagnosticsWriteDebugLogTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenClickScreenNodeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenCollapseNotificationShadeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenGetScreenNodeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenGetScreenTreeTool
import com.artrubadur.teno.agent.tools.impl.system.SystemGetClipboardTool
import com.artrubadur.teno.agent.tools.impl.system.SystemGetCurrentTimeTool
import com.artrubadur.teno.agent.tools.impl.system.SystemSetClipboardTool
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

    factory { DiagnosticsWriteDebugLogTool() } bind Tool::class
    factory { SystemGetCurrentTimeTool() } bind Tool::class
    factory { DeviceGetBrightnessTool(androidContext()) } bind Tool::class
    factory { DeviceSetBrightnessTool(androidContext()) } bind Tool::class
    factory { DeviceGetVolumeTool(androidContext()) } bind Tool::class
    factory { DeviceSetVolumeTool(androidContext()) } bind Tool::class
    factory { DeviceToggleFlashlightTool(androidContext()) } bind Tool::class
    factory { SystemGetClipboardTool(androidContext()) } bind Tool::class
    factory { SystemSetClipboardTool(androidContext()) } bind Tool::class
    factory { ScreenGetScreenTreeTool(get(), get()) } bind Tool::class
    factory { ScreenGetScreenNodeTool(get(), get()) } bind Tool::class
    factory { ScreenClickScreenNodeTool(get(), get()) } bind Tool::class
    factory { ScreenCollapseNotificationShadeTool() } bind Tool::class

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

