package com.artrubadur.tonemo.ui.overlays.bubble

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@Composable
fun OverlayRootContent(
    expanded: Boolean,
    onCollapseEnd: () -> Unit = {},
    onStop: () -> Unit = {},
) {
    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "overlay_progress",
        finishedListener = { if (it == 0f) onCollapseEnd() }
    )

    val shape = TopEndRevealShape(
        progress = progress
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
        ) {
            OverlayContent(
                onStop = onStop,
            )
        }
    }
}

@Preview
@Composable
private fun OverlayRootContentCollapsedPreview() {
    TonemoTheme {
        OverlayRootContent(false)
    }
}

@Preview
@Composable
private fun OverlayRootContentExpandedPreview() {
    TonemoTheme {
        OverlayRootContent(true)
    }
}
