package com.artrubadur.teno.ui.overlays.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
internal fun BoxScope.OnboardingCard(
    stepIndex: Int,
    finishing: Boolean,
    visibility: Float,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onFinish: () -> Unit,
) {
    AnimatedContent(
        targetState = stepIndex,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .fillMaxWidth()
            .graphicsLayer {
                translationX = if (finishing) -size.width * (1f - visibility) else 0f
            },
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (slideInHorizontally(tween(300)) { width -> direction * (width + 48) } togetherWith
                    slideOutHorizontally(tween(300)) { width -> -direction * (width + 48) }).using(
                null
            )
        },
        label = "Tour card",
        contentAlignment = Alignment.BottomCenter,
    ) { currentIndex ->
        val current = tourSteps[currentIndex]
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "${currentIndex + 1} / ${tourSteps.size}",
                    style = MaterialTheme.typography.labelMedium
                )
                Column(
                    Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(current.title, style = MaterialTheme.typography.titleLarge)
                    Text(current.text, style = MaterialTheme.typography.bodyMedium)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onFinish) { Text("Skip") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onBack, enabled = currentIndex > 0) { Text("Back") }
                    Button(onClick = if (currentIndex == tourSteps.lastIndex) onFinish else onNext) {
                        Text(if (currentIndex == tourSteps.lastIndex) "Finish" else "Next")
                    }
                }
            }
        }
    }
}
