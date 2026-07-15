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
import androidx.compose.ui.tooling.preview.Preview
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun AuraOverlay(
    modifier: Modifier = Modifier,
) {
    AuraOverlaySide(modifier, 1)
    AuraOverlaySide(modifier, 0)
}

@Composable
private fun AuraOverlaySide(
    modifier: Modifier = Modifier,
    side: Int
) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = modifier.fillMaxSize()) {
        val auraHeight = size.height * 0.9f
        val auraWidth = size.width * 0.4f

        val radius = auraWidth / 2f
        val scaleY = auraHeight / auraWidth

        val center = Offset(
            x = (side * (size.width + auraWidth * 0.2f)) - (auraWidth * 0.1f),
            y = size.height / 2f
        )

        withTransform({
            scale(
                scaleX = 1f,
                scaleY = scaleY,
                pivot = center
            )
        }) {
            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0f to color.copy(alpha = 0.8f),
                        0.25f to color.copy(alpha = 0.4f),
                        0.5f to color.copy(alpha = 0.2f),
                        0.75f to color.copy(alpha = 0.05f),
                        1f to Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

@Preview
@Composable
fun AuraOverlayPreview() {
    AppTheme {
        AuraOverlay()
    }
}

