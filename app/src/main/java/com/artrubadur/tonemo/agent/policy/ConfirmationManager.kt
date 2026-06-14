package com.artrubadur.tonemo.agent.policy

import com.artrubadur.tonemo.agent.orchestration.AgentSession
import com.artrubadur.tonemo.agent.tools.ToolCall
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ConfirmationManager {
    private val mutex = Mutex()
    private val pending = mutableMapOf<String, PendingConfirmation>()

    suspend fun create(
        session: AgentSession,
        call: ToolCall,
        title: String,
        description: String
    ): PendingConfirmation {
        val confirmation = PendingConfirmation(
            id = UUID.randomUUID().toString(),
            call = call,
            session = session,
            title = title,
            description = description
        )

        mutex.withLock {
            pending[confirmation.id] = confirmation
        }

        return confirmation
    }

    suspend fun consume(id: String): PendingConfirmation? {
        return mutex.withLock {
            pending.remove(id)
        }
    }
}


data class PendingConfirmation(
    val id: String,
    val call: ToolCall,
    val session: AgentSession,
    val title: String,
    val description: String
)
