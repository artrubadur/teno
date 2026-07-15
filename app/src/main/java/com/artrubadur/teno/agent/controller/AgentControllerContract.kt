package com.artrubadur.teno.agent.controller

import android.os.SystemClock
import com.artrubadur.teno.agent.orchestration.AgentEvent
import com.artrubadur.teno.connection.ConnectionKind
import kotlinx.serialization.Serializable

@Serializable
data class AgentControllerState(
    val activeConnectionId: String? = null,
    val activeConnectionName: String? = null,
    val activeConnectionKind: ConnectionKind? = null,
    val isReady: Boolean = false,
    val isLoading: Boolean = false,
    val isWorking: Boolean = false,
)

@Serializable
sealed interface AgentControllerCommand {
    @Serializable
    data object LaunchActiveConnection : AgentControllerCommand

    @Serializable
    data object TerminateConnection : AgentControllerCommand

    @Serializable
    data object StopWork : AgentControllerCommand

    @Serializable
    data class SendMessage(val prompt: String) : AgentControllerCommand

    @Serializable
    data class ApproveConfirmation(val confirmationId: String) : AgentControllerCommand

    @Serializable
    data class RejectConfirmation(val confirmationId: String) : AgentControllerCommand
}

@Serializable
sealed interface AgentControllerEvent {
    val time: Long

    @Serializable
    data class StateChanged(
        val state: AgentControllerState,
        override val time: Long = SystemClock.elapsedRealtime(),
    ) : AgentControllerEvent

    @Serializable
    data class Message(
        val message: String,
        override val time: Long = SystemClock.elapsedRealtime(),
    ) : AgentControllerEvent

    @Serializable
    data class Agent(
        val event: AgentEvent,
        override val time: Long = SystemClock.elapsedRealtime(),
    ) : AgentControllerEvent
}
