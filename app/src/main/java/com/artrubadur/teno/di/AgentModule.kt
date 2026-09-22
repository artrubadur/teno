package com.artrubadur.teno.di

import com.artrubadur.teno.agent.orchestration.AgentOrchestrator
import com.artrubadur.teno.agent.policy.ConfirmationManager
import com.artrubadur.teno.agent.policy.SafetyPolicy
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolBroker
import com.artrubadur.teno.agent.tools.ToolManager
import com.artrubadur.teno.agent.tools.ToolRegistry
import com.artrubadur.teno.agent.tools.impl.apps.AppsGetInfoTool
import com.artrubadur.teno.agent.tools.impl.apps.AppsListInstalledTool
import com.artrubadur.teno.agent.tools.impl.apps.AppsOpenTool
import com.artrubadur.teno.agent.tools.impl.apps.AppsSearchTool
import com.artrubadur.teno.agent.tools.impl.calendar.CalendarCreateEventTool
import com.artrubadur.teno.agent.tools.impl.calendar.CalendarDeleteEventTool
import com.artrubadur.teno.agent.tools.impl.calendar.CalendarSearchEventsTool
import com.artrubadur.teno.agent.tools.impl.calendar.CalendarUpdateEventTool
import com.artrubadur.teno.agent.tools.impl.contacts.ContactsCreateTool
import com.artrubadur.teno.agent.tools.impl.contacts.ContactsDeleteTool
import com.artrubadur.teno.agent.tools.impl.contacts.ContactsGetTool
import com.artrubadur.teno.agent.tools.impl.contacts.ContactsSearchTool
import com.artrubadur.teno.agent.tools.impl.contacts.ContactsUpdateTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceGetBrightnessTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceGetVolumeTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceSetBrightnessTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceSetVolumeTool
import com.artrubadur.teno.agent.tools.impl.device.DeviceToggleFlashlightTool
import com.artrubadur.teno.agent.tools.impl.diagnostics.DiagnosticsWriteDebugLogTool
import com.artrubadur.teno.agent.tools.impl.phone.PhoneCallTool
import com.artrubadur.teno.agent.tools.impl.phone.PhoneGetCallHistoryTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenClickScreenNodeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenCollapseNotificationShadeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenGetScreenNodeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenGetScreenTreeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenLongClickScreenNodeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenPressEnterTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenPressNavigationButtonTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenScrollToEdgeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenScrollToNodeTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenSetTextTool
import com.artrubadur.teno.agent.tools.impl.screen.ScreenSwipeTool
import com.artrubadur.teno.agent.tools.impl.sms.SmsSearchTool
import com.artrubadur.teno.agent.tools.impl.sms.SmsSendTool
import com.artrubadur.teno.agent.tools.impl.system.SystemGetClipboardTool
import com.artrubadur.teno.agent.tools.impl.system.SystemGetCurrentTimeTool
import com.artrubadur.teno.agent.tools.impl.system.SystemSetClipboardTool
import com.artrubadur.teno.agent.tools.impl.system.SystemWaitTool
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenEdgeScroller
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenEnterPresser
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeClicker
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeScroller
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeSwiper
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeTextInputter
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenTreeReader
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val agentModule = module {
    single { ScreenNodeStore() }
    single { ScreenTreeReader(androidContext()) }
    single { ScreenNodeClicker(get()) }
    single { ScreenNodeSwiper(androidContext(), get()) }
    single { ScreenNodeScroller(get()) }
    single { ScreenEdgeScroller(get()) }
    single { ScreenNodeTextInputter(get()) }
    single { ScreenEnterPresser(get()) }

    factory { ScreenGetScreenTreeTool(get(), get()) } bind Tool::class
    factory { ScreenGetScreenNodeTool(get(), get()) } bind Tool::class
    factory { ScreenClickScreenNodeTool(get(), get()) } bind Tool::class
    factory { ScreenLongClickScreenNodeTool(get(), get()) } bind Tool::class
    factory { ScreenScrollToNodeTool(get(), get()) } bind Tool::class
    factory { ScreenScrollToEdgeTool(get()) } bind Tool::class
    factory { ScreenCollapseNotificationShadeTool() } bind Tool::class
    factory { ScreenSwipeTool(get(), get()) } bind Tool::class
    factory { ScreenPressNavigationButtonTool() } bind Tool::class
    factory { ScreenSetTextTool(get(), get()) } bind Tool::class
    factory { ScreenPressEnterTool(get()) } bind Tool::class

    factory { DeviceGetBrightnessTool(androidContext()) } bind Tool::class
    factory { DeviceSetBrightnessTool(androidContext()) } bind Tool::class
    factory { DeviceGetVolumeTool(androidContext()) } bind Tool::class
    factory { DeviceSetVolumeTool(androidContext()) } bind Tool::class
    factory { DeviceToggleFlashlightTool(androidContext()) } bind Tool::class

    factory { SystemGetCurrentTimeTool() } bind Tool::class
    factory { SystemGetClipboardTool(androidContext()) } bind Tool::class
    factory { SystemSetClipboardTool(androidContext()) } bind Tool::class
    factory { SystemWaitTool() } bind Tool::class

    factory { ContactsSearchTool(androidContext()) } bind Tool::class
    factory { ContactsGetTool(androidContext()) } bind Tool::class
    factory { ContactsCreateTool(androidContext()) } bind Tool::class
    factory { ContactsUpdateTool(androidContext()) } bind Tool::class
    factory { ContactsDeleteTool(androidContext()) } bind Tool::class

    factory { PhoneCallTool(androidContext()) } bind Tool::class
    factory { PhoneGetCallHistoryTool(androidContext()) } bind Tool::class

    factory { SmsSendTool(androidContext()) } bind Tool::class
    factory { SmsSearchTool(androidContext()) } bind Tool::class

    factory { AppsListInstalledTool(androidContext()) } bind Tool::class
    factory { AppsOpenTool(androidContext()) } bind Tool::class
    factory { AppsSearchTool(androidContext()) } bind Tool::class
    factory { AppsGetInfoTool(androidContext()) } bind Tool::class

    factory { CalendarSearchEventsTool(androidContext()) } bind Tool::class
    factory { CalendarCreateEventTool(androidContext()) } bind Tool::class
    factory { CalendarUpdateEventTool(androidContext()) } bind Tool::class
    factory { CalendarDeleteEventTool(androidContext()) } bind Tool::class

    factory { DiagnosticsWriteDebugLogTool() } bind Tool::class

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
