package com.artrubadur.tonemo.ui.screens.onboarding

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.artrubadur.tonemo.overlay.OverlayService
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var overlayEnabled by remember { mutableStateOf(OverlayService.isRunning) }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val granted = Settings.canDrawOverlays(context)
        overlayEnabled = granted
        if (granted) {
            OverlayService.start(context)
        }
    }

    LaunchedEffect(Unit) {
        overlayEnabled = OverlayService.isRunning
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Enable overlay")
            Switch(
                checked = overlayEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        if (Settings.canDrawOverlays(context)) {
                            overlayEnabled = true
                            OverlayService.start(context)
                        } else {
                            overlayEnabled = false
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                "package:${context.packageName}".toUri()
                            )
                            overlayPermissionLauncher.launch(intent)
                        }
                    } else {
                        overlayEnabled = false
                        OverlayService.stop(context)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    TonemoTheme {
        OnboardingScreen()
    }
}
