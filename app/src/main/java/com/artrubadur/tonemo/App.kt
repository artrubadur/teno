package com.artrubadur.tonemo

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artrubadur.tonemo.ui.screens.onboarding.OnboardingScreen

@Composable
fun App() {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        OnboardingScreen(modifier = Modifier.padding(innerPadding))
    }
}
