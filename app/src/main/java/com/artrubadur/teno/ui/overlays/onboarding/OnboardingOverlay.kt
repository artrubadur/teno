package com.artrubadur.teno.ui.overlays.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot

@Composable
fun OnboardingOverlay(
    stepIndex: Int,
    targets: TourTargets,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    val step = tourSteps[stepIndex]
    var origin by remember { mutableStateOf(Offset.Zero) }
    var finishing by remember { mutableStateOf(false) }
    val visibility = remember { Animatable(0f) }

    LaunchedEffect(finishing) {
        visibility.animateTo(if (finishing) 0f else 1f, tween(300))
        if (finishing) onFinish()
    }

    val target = step.target?.let { targets.bounds[it]?.translate(-origin) }
    val targetShape = step.target?.let { targets.shapes[it] }
    val requestFinish = { finishing = true }
    val openTarget = {
        step.target?.let { targets.actions[it]?.invoke() }
        onNext()
    }

    BackHandler { if (stepIndex > 0) onBack() else requestFinish() }

    Box(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { origin = it.positionInRoot() },
    ) {
        OnboardingSpotlight(
            step = step,
            target = target,
            targetShape = targetShape,
            visibility = visibility.value,
            onOpenTarget = openTarget,
        )
        OnboardingCard(
            stepIndex = stepIndex,
            finishing = finishing,
            visibility = visibility.value,
            onNext = onNext,
            onBack = onBack,
            onFinish = requestFinish,
        )
    }
}
