package com.artrubadur.teno.ui.screens.tools

import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolSpec

data class ToolsState(
    val tools: List<ToolItemState> = emptyList(),
)

data class ToolItemState(
    val spec: ToolSpec,
    val enabled: Boolean,
    val permissions: List<ToolPermissionState> = emptyList(),
)

data class ToolPermissionState(
    val permission: ToolPermission,
    val granted: Boolean,
)
