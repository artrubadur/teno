package com.artrubadur.teno.ui.screens.home

import android.Manifest
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.ConnectionManager
import com.artrubadur.teno.connection.ConnectionType
import com.artrubadur.teno.ui.overlay.OverlayForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val application: Application,
    connectionManager: ConnectionManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    val activeConnection: StateFlow<Connection?> =
        connectionManager
            .observeActiveConnection(ConnectionType.LLM)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null
            )

    private val overlayStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != OverlayForegroundService.ACTION_STATE) return
            applyOverlayRunning(
                intent.getBooleanExtra(OverlayForegroundService.EXTRA_IS_RUNNING, false)
            )
        }
    }

    init {
        ContextCompat.registerReceiver(
            application,
            overlayStateReceiver,
            IntentFilter(OverlayForegroundService.ACTION_STATE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        refresh()
    }

    fun refresh() {
        refreshPermissions()
        verifyOverlayRunning()
    }

    private fun refreshPermissions() {
        _state.update {
            it.copy(
                overlayPermissionGranted = Settings.canDrawOverlays(application),
                notificationPermissionGranted = hasNotificationPermission(),
            )
        }
    }

    fun setOverlayEnabled(enabled: Boolean): HomeCommand? {
        refreshPermissions()

        if (!enabled) {
            _state.update { it.copy(overlayChanging = true) }
            OverlayForegroundService.shutdown(application)
            applyOverlayRunning(false)
            return null
        }

        if (!_state.value.overlayPermissionGranted) {
            return HomeCommand.RequestOverlayPermission
        }

        if (!state.value.notificationPermissionGranted) {
            return HomeCommand.RequestNotificationPermission
        }

        _state.update { it.copy(overlayChanging = true) }
        OverlayForegroundService.start(application)

        return null
    }

    fun retryEnableOverlay(): HomeCommand? = setOverlayEnabled(true)

    private fun verifyOverlayRunning() {
        if (_state.value.overlayChanging) return
        OverlayForegroundService.requestRunningState(application)
    }

    private fun applyOverlayRunning(isRunning: Boolean) {
        _state.update {
            it.copy(
                overlayEnabled = isRunning,
                overlayChanging = false,
            )
        }
    }

    override fun onCleared() {
        application.unregisterReceiver(overlayStateReceiver)
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }
}

sealed interface HomeCommand {
    data object RequestOverlayPermission : HomeCommand
    data object RequestNotificationPermission : HomeCommand
}
