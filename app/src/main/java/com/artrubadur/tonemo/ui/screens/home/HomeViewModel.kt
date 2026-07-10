package com.artrubadur.tonemo.ui.screens.home

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.artrubadur.tonemo.ui.overlay.OverlayForegroundService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel(
    private val application: Application,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var pendingEnable = false

    init {
        refresh()
    }

    fun refresh() {
        _state.update {
            it.copy(
                overlayPermissionGranted = Settings.canDrawOverlays(application),
                notificationPermissionGranted = hasNotificationPermission(),
            )
        }
    }

    fun setOverlayEnabled(): HomeCommand? {
        refresh()
        pendingEnable = true

        if (!_state.value.overlayPermissionGranted) {
            return HomeCommand.RequestOverlayPermission
        }

        if (!state.value.notificationPermissionGranted) {
            return HomeCommand.RequestNotificationPermission
        }

        OverlayForegroundService.start(application)
        pendingEnable = false

        return null
    }

    fun onOverlayPermissionResult() {
        refresh()

        if (!pendingEnable) return
        if (!state.value.overlayPermissionGranted) {
            pendingEnable = false
            return
        }

        setOverlayEnabled()
    }

    fun onNotificationPermissionResult() {
        refresh()

        if (!pendingEnable) return
        if (!state.value.notificationPermissionGranted) {
            pendingEnable = false
            return
        }

        setOverlayEnabled()
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    application,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }
}

data class HomeState(
    val overlayPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = true,
)

sealed interface HomeCommand {
    data object RequestOverlayPermission : HomeCommand
    data object RequestNotificationPermission : HomeCommand
}
