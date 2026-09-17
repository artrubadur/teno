package com.artrubadur.teno.ui.screens.help.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.buttons.PlainIconButton

@Composable
internal fun GuideItem(
    title: String,
    text: String,
    dividerAfter: Boolean = true,
    links: @Composable () -> Unit = {},
) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) -90f else 90f,
        animationSpec = tween(150),
        label = "FAQ arrow",
    )
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (expanded) "Collapse answer" else "Expand answer",
                ) { expanded = !expanded }
                .semantics { stateDescription = if (expanded) "Expanded" else "Collapsed" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            PlainIconButton(
                iconRes = R.drawable.ic_kb_arrow,
                contentDescription = if (expanded) "Collapse answer" else "Expand answer",
                onClick = { expanded = !expanded },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                iconModifier = Modifier
                    .size(ButtonDefaults.IconSize)
                    .rotate(arrowRotation),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(expandFrom = Alignment.Top, animationSpec = tween(150)) +
                    fadeIn(animationSpec = tween(150)),
            exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = tween(150)) +
                    fadeOut(animationSpec = tween(150)),
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                links()
            }
        }
        if (dividerAfter) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}
