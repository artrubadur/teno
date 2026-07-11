package com.artrubadur.tonemo.ui.screens.home

data class HomeState(
    val overlayPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = true,
)