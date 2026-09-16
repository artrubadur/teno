package com.artrubadur.teno.ui.overlay.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform

@Composable
fun AuraOverlay() {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxSize()) {
        val width = size.width * 0.5f
        val radius = width / 2f
        val scaleY = size.height * 0.9f / width
        for (side in 0..1) {
            val center = Offset(
                side * (size.width + width * 0.2f) - width * 0.1f,
                size.height / 2f,
            )
            withTransform({ scale(1f, scaleY, center) }) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0f to color.copy(alpha = 0.8f),
                            0.25f to color.copy(alpha = 0.4f),
                            0.5f to color.copy(alpha = 0.2f),
                            0.75f to color.copy(alpha = 0.05f),
                            1f to Color.Transparent,
                        ),
                        center = center,
                        radius = radius,
                    ),
                    radius = radius,
                    center = center,
                )
            }
        }
    }
}
