package com.artrubadur.tonemo.ui.screens.tools

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.tonemo.agent.tools.ToolManager
import com.artrubadur.tonemo.agent.tools.ToolPermission
import com.artrubadur.tonemo.agent.tools.ToolSpec
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ToolsViewModel(
    private val application: Application,
    private val toolManager: ToolManager,
) : ViewModel() {

    private val _state = MutableStateFlow(ToolsState())
    val state: StateFlow<ToolsState> = _state.asStateFlow()

    private val _permissions = MutableSharedFlow<ToolPermission>(extraBufferCapacity = 1)
    val permissions: SharedFlow<ToolPermission> = _permissions.asSharedFlow()

    private var enabledNames: Set<String> = emptySet()
    private var pendingToolName: String? = null
    private var pendingPermissions: List<ToolPermission> = emptyList()
    private var pendingPermission: ToolPermission? = null

    init {
        toolManager.enabledToolNames
            .onEach { names ->
                enabledNames = names
                refresh()
            }
            .launchIn(viewModelScope)
    }

    fun setToolEnabled(toolName: String, enabled: Boolean) {
        if (!enabled) {
            viewModelScope.launch {
                toolManager.setEnabled(toolName, false)
            }
            return
        }

        val spec = toolManager.allSpecs().firstOrNull { it.name == toolName } ?: return
        val missingPermissions = spec.missingPermissions()
        if (missingPermissions.isEmpty()) {
            viewModelScope.launch {
                toolManager.setEnabled(toolName, true)
            }
            return
        }

        pendingToolName = toolName
        pendingPermissions = missingPermissions
        requestNextPermission()
    }

    fun grantPermission(permission: ToolPermission) {
        pendingPermission = permission
        _permissions.tryEmit(permission)
    }

    fun onPermissionResult() {
        val permission = pendingPermission
        if (permission != null) {
            pendingPermission = null
            refresh()
            return
        }

        val toolName = pendingToolName ?: return
        val requestedPermission = pendingPermissions.firstOrNull() ?: return
        if (!requestedPermission.isGranted(application)) {
            clearPendingEnable()
            refresh()
            return
        }

        pendingPermissions = pendingPermissions.drop(1).filterNot { it.isGranted(application) }
        if (pendingPermissions.isEmpty()) {
            viewModelScope.launch {
                toolManager.setEnabled(toolName, true)
                clearPendingEnable()
            }
        } else {
            requestNextPermission()
        }
        refresh()
    }

    private fun requestNextPermission() {
        val permission = pendingPermissions.firstOrNull()
        if (permission == null) {
            val toolName = pendingToolName ?: return
            viewModelScope.launch {
                toolManager.setEnabled(toolName, true)
                clearPendingEnable()
            }
            return
        }
        _permissions.tryEmit(permission)
    }

    private fun ToolSpec.missingPermissions(): List<ToolPermission> {
        return requiredPermissions.filterNot { it.isGranted(application) }
    }

    fun refresh() {
        _state.update {
            ToolsState(
                tools = toolManager.allSpecs().map { spec ->
                    val permissions = spec.requiredPermissions.map { permission ->
                        ToolPermissionState(
                            permission = permission,
                            granted = permission.isGranted(application)
                        )
                    }
                    ToolItemState(
                        spec = spec,
                        enabled = spec.name in enabledNames && permissions.all { it.granted },
                        permissions = permissions,
                    )
                }
            )
        }
    }

    private fun clearPendingEnable() {
        pendingToolName = null
        pendingPermissions = emptyList()
    }
}
