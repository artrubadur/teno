package com.artrubadur.teno.ui.screens.connections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.ui.components.SectionLabel

@Composable
fun ConnectionList(
    modifier: Modifier,
    connections: List<Connection> = listOf(),
    onOpenConnection: (String) -> Unit = {},
    onToggleActive: (Connection) -> Unit = {},
) {
    val groupedConnections = connections
        .groupBy { it.kind }
        .toList()
        .sortedBy { (kind, _) -> kind.ordinal }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedConnections.forEach { (kind, connections) ->
            item(key = kind.name) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SectionLabel(text = kind.name)

                    connections.forEach { connection ->
                        ConnectionCard(
                            connection = connection,
                            onOpenConnection = onOpenConnection,
                            onToggleActive = onToggleActive,
                        )
                    }
                }
            }
        }
    }
}
