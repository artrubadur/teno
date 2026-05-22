package com.artrubadur.tonemo.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun OverlayRootContent(
    expanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        ),
        label = "overlay_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clipToBounds(),
        contentAlignment = Alignment.TopEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp)
                .graphicsLayer {
                    alpha = progress
                    scaleX = 0.05f + 0.95f * progress
                    scaleY = 0.05f + 0.95f * progress
                    transformOrigin = TransformOrigin(1f, 0f)
                }
        ) {
            OverlayContent(
                onCollapse = onCollapse
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 0.dp)
                .graphicsLayer {
                    alpha = 1f - progress
                    scaleX = 1f - 0.25f * progress
                    scaleY = 1f - 0.25f * progress
                    transformOrigin = TransformOrigin(1f, 0f)
                }
        ) {
            AssistantOverlayContent(
                onExpand = onExpand
            )
        }
    }
}