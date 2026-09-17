package com.artrubadur.teno

import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.artrubadur.teno.data.agent.AgentInstructionKind
import com.artrubadur.teno.ui.overlays.onboarding.LocalTourTargets
import com.artrubadur.teno.ui.overlays.onboarding.OnboardingOverlay
import com.artrubadur.teno.ui.overlays.onboarding.TourTargets
import com.artrubadur.teno.ui.overlays.onboarding.tourSteps
import com.artrubadur.teno.ui.screens.chat.ChatScreen
import com.artrubadur.teno.ui.screens.connections.ConnectionsScreen
import com.artrubadur.teno.ui.screens.connections.details.ConnectionDetailsScreen
import com.artrubadur.teno.ui.screens.help.HelpScreen
import com.artrubadur.teno.ui.screens.home.HomeScreen
import com.artrubadur.teno.ui.screens.settings.SettingsScreen
import com.artrubadur.teno.ui.screens.settings.instructions.AgentInstructionsScreen
import com.artrubadur.teno.ui.screens.tools.ToolsScreen

@Composable
fun App() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("onboarding", Context.MODE_PRIVATE) }
    var tourStep by rememberSaveable {
        mutableIntStateOf(if (preferences.getBoolean("completed", false)) -1 else 0)
    }
    val targets = remember { TourTargets() }
    fun finishTour() {
        preferences.edit { putBoolean("completed", true) }
        tourStep = -1
    }
    LaunchedEffect(tourStep) {
        if (tourStep >= 0) {
            val route = tourSteps[tourStep].route
            if (navController.currentBackStackEntry?.destination?.route != route) {
                navController.navigate(route) {
                    popUpTo(Route.Home)
                    launchSingleTop = true
                }
            }
        }
    }

    CompositionLocalProvider(LocalTourTargets provides targets) {
        Box(Modifier.fillMaxSize()) {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                NavHost(
                    modifier = Modifier.padding(innerPadding),
                    navController = navController,
                    startDestination = Route.Home,
                    enterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(300)
                        ) + fadeOut(animationSpec = tween(300))
                    }
                ) {
                    composable(Route.Chat) {
                        ChatScreen(
                            onBack = { navController.popBackStack() },
                            onOpenConnections = { navController.navigate(Route.Connections) },
                        )
                    }
                    composable(Route.Home) {
                        HomeScreen(
                            onOpenChat = { navController.navigate(Route.Chat) },
                            onOpenConnections = { navController.navigate(Route.Connections) },
                            onOpenSettings = { navController.navigate(Route.Settings) },
                            onOpenTools = { navController.navigate(Route.Tools) },
                            onOpenHelp = { navController.navigate(Route.Help) },
                        )
                    }
                    composable(Route.Connections) {
                        ConnectionsScreen(
                            onBack = { navController.popBackStack() },
                            onOpenConnection = { id ->
                                navController.navigate(
                                    Route.connectionDetails(
                                        id
                                    )
                                )
                            }
                        )
                    }
                    composable(Route.Help) {
                        HelpScreen(
                            onBack = { navController.popBackStack() },
                            onRestartTour = { tourStep = 0 },
                        )
                    }
                    composable(Route.ConnectionDetails) { backStackEntry ->
                        ConnectionDetailsScreen(
                            onBack = { navController.popBackStack() },
                            connectionId = backStackEntry.arguments?.getString(Route.ConnectionId),
                        )
                    }
                    composable(Route.Tools) {
                        ToolsScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(Route.Settings) {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onOpenTools = { navController.navigate(Route.Tools) },
                            onOpenIdentity = { navController.navigate(Route.SettingsIdentity) },
                            onOpenRules = { navController.navigate(Route.SettingsRules) },
                        )
                    }
                    composable(Route.SettingsIdentity) {
                        AgentInstructionsScreen(
                            kind = AgentInstructionKind.IDENTITY,
                            onBack = { navController.popBackStack() },
                        )
                    }
                    composable(Route.SettingsRules) {
                        AgentInstructionsScreen(
                            kind = AgentInstructionKind.RULES,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
            if (tourStep >= 0) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    OnboardingOverlay(
                        stepIndex = tourStep,
                        targets = targets,
                        onNext = { tourStep++ },
                        onBack = { if (tourStep > 0) tourStep-- },
                        onFinish = ::finishTour,
                    )
                }
            }
        }
    }
}

private object Route {
    const val Home = "home"
    const val Help = "help"
    const val Connections = "connections"
    const val ConnectionId = "connectionId"
    const val ConnectionDetails = "connections/{$ConnectionId}"
    const val Chat = "chat"
    const val Settings = "settings"
    const val Tools = "settings/tools"
    const val SettingsIdentity = "settings/identity"
    const val SettingsRules = "settings/rules"

    fun connectionDetails(id: String) = "connections/${Uri.encode(id)}"
}
