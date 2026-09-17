package com.artrubadur.teno.ui.overlays.onboarding

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

class TourTargets {
    val bounds = mutableStateMapOf<String, Rect>()
    val shapes = mutableStateMapOf<String, Shape>()
    val actions = mutableMapOf<String, () -> Unit>()
}

val LocalTourTargets = staticCompositionLocalOf<TourTargets?> { null }

@Composable
fun Modifier.tourTarget(
    id: String,
    shape: Shape = RoundedCornerShape(16.dp),
    visualSize: DpSize? = null,
    action: (() -> Unit)? = null,
): Modifier {
    val targets = LocalTourTargets.current ?: return this
    val density = LocalDensity.current
    val currentAction by rememberUpdatedState(action)
    DisposableEffect(targets, id, shape) {
        val registeredAction = { currentAction?.invoke(); Unit }
        targets.shapes[id] = shape
        targets.actions[id] = registeredAction
        onDispose {
            if (targets.actions[id] === registeredAction) {
                targets.bounds.remove(id)
                targets.shapes.remove(id)
                targets.actions.remove(id)
            }
        }
    }
    return onGloballyPositioned {
        val bounds = it.boundsInRoot()
        targets.bounds[id] = if (visualSize == null) bounds else with(density) {
            val size = Size(visualSize.width.toPx(), visualSize.height.toPx())
            Rect(bounds.center - Offset(size.width / 2f, size.height / 2f), size)
        }
    }
}

