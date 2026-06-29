package com.artrubadur.tonemo

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.artrubadur.tonemo.ui.screens.chat.ChatScreen
import com.artrubadur.tonemo.ui.screens.connections.ConnectionsScreen
import com.artrubadur.tonemo.ui.screens.onboarding.OnboardingScreen

@Composable
fun App() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = Route.Onboarding,
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
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Route.Onboarding) {
                OnboardingScreen(
                    onOpenChat = { navController.navigate(Route.Chat) },
                    onOpenConnections = { navController.navigate(Route.Connections) }
                )
            }
            composable(Route.Connections) {
                ConnectionsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private object Route {
    const val Onboarding = "onboarding"
    const val Connections = "connections"
    const val Chat = "chat"
}
