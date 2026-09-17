package com.artrubadur.teno.ui.overlays.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun OnboardingSpotlight(
    step: TourStep,
    target: Rect?,
    targetShape: Shape?,
    visibility: Float,
    onOpenTarget: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    Canvas(
        Modifier
            .fillMaxSize()
            .semantics {
                contentDescription =
                    step.target?.let { "Highlighted $it button" } ?: "Tour background"
                if (target != null) onClick("Open ${step.target}") { onOpenTarget(); true }
            }
            .pointerInput(step.title, target) {
                detectTapGestures { position ->
                    if (target?.contains(position) == true) onOpenTarget()
                }
            },
    ) {
        val mask = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(Offset.Zero, size))
            if (target != null) {
                val padding = 2.dp.toPx()
                val outline = targetShape?.createOutline(target.size, layoutDirection, this@Canvas)
                val corners = (outline as? Outline.Rounded)?.roundRect
                fun expanded(radius: CornerRadius?) = CornerRadius(
                    (radius?.x ?: 0f) + padding,
                    (radius?.y ?: 0f) + padding,
                )
                addRoundRect(
                    RoundRect(
                        rect = target.inflate(padding),
                        topLeft = expanded(corners?.topLeftCornerRadius),
                        topRight = expanded(corners?.topRightCornerRadius),
                        bottomRight = expanded(corners?.bottomRightCornerRadius),
                        bottomLeft = expanded(corners?.bottomLeftCornerRadius),
                    )
                )
            }
        }
        drawPath(mask, Color.Black.copy(alpha = 0.76f * visibility))
    }
}
