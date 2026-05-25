package com.artrubadur.tonemo.overlay

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

class TopEndRevealShape(
    private val progress: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val (collapsedSize, collapsedCorner, expandedCorner) = with(density) {
            Triple(
                28.dp.toPx(),
                14.dp.toPx(),
                12.dp.toPx()
            )
        }

        val width = lerpFloat(collapsedSize, size.width, progress)
        val height = lerpFloat(collapsedSize, size.height, progress)
        val corner = lerpFloat(collapsedCorner, expandedCorner, progress)

        val left = size.width - width
        val top = 0f
        val right = size.width

        return Outline.Rounded(
            RoundRect(
                rect = Rect(left, top, right, height),
                cornerRadius = CornerRadius(corner)
            )
        )
    }
}

private fun lerpFloat(
    start: Float,
    stop: Float,
    fraction: Float
): Float {
    return start + (stop - start) * fraction
}
