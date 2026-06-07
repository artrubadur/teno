package com.artrubadur.tonemo.ui.overlays.bubble

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@Composable
fun BubbleContent(
    expanded: Boolean,
    onCollapseAnimationEnd: () -> Unit = {},
) {
    val animationSpec = tween<Float>(
        durationMillis = 200,
        easing = FastOutSlowInEasing
    )

    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = animationSpec,
        label = "bubble_progress",
        finishedListener = { if (!expanded) onCollapseAnimationEnd() }
    )

    val bubbleSize = lerp(28.dp, 40.dp, progress)
    val bubbleYOffset = lerp(0.dp, 20.dp, progress)
    val bubbleXOffset = lerp(0.dp, (-16).dp, progress)
    val corner = lerp(16.dp, 8.dp, progress)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = bubbleXOffset.roundToPx(),
                        y = bubbleYOffset.roundToPx()
                    )
                }
                .size(bubbleSize)
                .let { base ->
                    if (progress == 1f) base else base.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(corner)
                    )
                },
            shape = RoundedCornerShape(corner),
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_expand),
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

@Preview
@Composable
private fun BubbleContentExpandedPreview() {
    TonemoTheme {
        BubbleContent(true)
    }
}

@Preview
@Composable
private fun BubbleContentCollapsedPreview() {
    TonemoTheme {
        BubbleContent(false)
    }
}
