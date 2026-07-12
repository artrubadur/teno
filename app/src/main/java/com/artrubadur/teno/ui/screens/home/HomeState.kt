package com.artrubadur.teno.ui.screens.home

data class HomeState(
    val overlayPermissionGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = true,
)