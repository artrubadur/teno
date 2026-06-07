package com.artrubadur.tonemo.ui.screens.modelManager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.data.model.ModelMetadata
import com.artrubadur.tonemo.data.model.ModelType
import com.artrubadur.tonemo.data.model.StoredModel
import com.artrubadur.tonemo.ui.components.buttons.PrimaryIconButton
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@Composable
fun ModelCard(
    model: StoredModel,
    isActive: Boolean,
    onClick: ((model: StoredModel) -> Unit)?,
    onToggleActive: ((model: StoredModel) -> Unit)
) {
    Card(
        onClick = { onClick?.invoke(model) },
        modifier = Modifier.fillMaxWidth(),
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = model.metadata.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = model.metadata.modelType.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isActive) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            PrimaryIconButton(
                iconRes = if (isActive) R.drawable.ic_close else R.drawable.ic_confirm,
                contentDescription = if (isActive) "Activate" else "Deactivate",
                onClick = { onToggleActive(model) },
                enabled = onClick == null
            )
        }
    }
}

@Preview
@Composable
private fun ModelCardActivePreview() {
    TonemoTheme {
        ModelCard(
            model = StoredModel(
                modelFile = java.io.File("1.litertlm"),
                metadata = ModelMetadata(
                    modelType = ModelType.LLM,
                    modelFileName = "1.litertlm",
                    displayName = "Tiny Llama",
                    uploadedAt = 0L
                )
            ),
            isActive = true,
            onClick = null,
            onToggleActive = {}
        )
    }
}

@Preview
@Composable
private fun ModelCardInactivePreview() {
    TonemoTheme {
        ModelCard(
            model = StoredModel(
                modelFile = java.io.File("1.litertlm"),
                metadata = ModelMetadata(
                    modelType = ModelType.LLM,
                    modelFileName = "1.litertlm",
                    displayName = "Tiny Llama",
                    uploadedAt = 0L
                )
            ),
            isActive = false,
            onClick = null,
            onToggleActive = {}
        )
    }
}
