package com.artrubadur.teno.agent.orchestration

import com.artrubadur.teno.agent.policy.ConfirmationManager
import com.artrubadur.teno.agent.policy.SafetyPolicy
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolBroker
import com.artrubadur.teno.agent.tools.ToolCall
import com.artrubadur.teno.agent.tools.ToolRegistry
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.runtime.llm.LlmMessage
import com.artrubadur.teno.connection.runtime.llm.LlmRequest
import com.artrubadur.teno.connection.runtime.llm.LlmResponse
import com.artrubadur.teno.connection.runtime.llm.LlmRuntime
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentOrchestratorTest {

    @Test
    fun `runs tool loop until final answer`() = runBlocking {
        val runtime = FakeLlmRuntime(
            responses = mutableListOf(
                LlmResponse.ToolCalls(
                    listOf(ToolCall(tool = "echo", id = "id", arguments = buildJsonObject { }))
                ),
                LlmResponse.Final("done")
            ),
        )
        val tool = EchoTool()
        val orchestrator = AgentOrchestrator(
            llmRuntime = runtime,
            toolBroker = ToolBroker(
                registry = ToolRegistry(listOf(tool)),
                safetyPolicy = SafetyPolicy()
            ),
            confirmationManager = ConfirmationManager(),
        )

        val events = orchestrator.handleUserMessage("hello").toList()

        assertEquals(3, events.size)
        assertTrue(events[0] is AgentEvent.ToolStarted)
        assertTrue(events[1] is AgentEvent.ToolExecuted)
        assertTrue(events[2] is AgentEvent.FinalAnswer)
        assertEquals(1, tool.executions)
    }

    @Test
    fun `continues after confirmation approval`() = runBlocking {
        val runtime = FakeLlmRuntime(
            responses = mutableListOf(
                LlmResponse.ToolCalls(
                    listOf(ToolCall(tool = "confirm", id = "id", arguments = buildJsonObject { }))
                ),
                LlmResponse.Final("approved")
            ),
        )
        val tool = ConfirmTool()
        val orchestrator = AgentOrchestrator(
            llmRuntime = runtime,
            toolBroker = ToolBroker(
                registry = ToolRegistry(listOf(tool)),
                safetyPolicy = SafetyPolicy()
            ),
            confirmationManager = ConfirmationManager(),
        )

        val firstRun = orchestrator.handleUserMessage("confirm this").toList()
        val confirmation = firstRun.filterIsInstance<AgentEvent.ConfirmationRequired>().single()

        assertEquals(0, tool.executions)

        val secondRun = orchestrator.approveConfirmation(confirmation.confirmationId).toList()

        assertTrue(secondRun[0] is AgentEvent.ToolExecuted)
        assertTrue(secondRun.last() is AgentEvent.FinalAnswer)
        assertEquals(1, tool.executions)
    }

    @Test
    fun `rejecting confirmation reports rejected tool`() = runBlocking {
        val runtime = FakeLlmRuntime(
            responses = mutableListOf(
                LlmResponse.ToolCalls(
                    listOf(ToolCall(tool = "confirm", id = "id", arguments = buildJsonObject { }))
                ),
                LlmResponse.Final("rejected")
            ),
        )
        val tool = ConfirmTool()
        val orchestrator = AgentOrchestrator(
            llmRuntime = runtime,
            toolBroker = ToolBroker(
                registry = ToolRegistry(listOf(tool)),
                safetyPolicy = SafetyPolicy()
            ),
            confirmationManager = ConfirmationManager(),
        )

        val firstRun = orchestrator.handleUserMessage("reject this").toList()
        val confirmation = firstRun.filterIsInstance<AgentEvent.ConfirmationRequired>().single()

        val events = orchestrator.rejectConfirmation(confirmation.confirmationId).toList()
        val event = events.first() as AgentEvent.ToolBlocked

        assertEquals(tool.name, event.result.tool)
        assertTrue(events.last() is AgentEvent.FinalAnswer)
        assertEquals(0, tool.executions)
    }

    private class FakeLlmRuntime(
        private val responses: MutableList<LlmResponse>
    ) : LlmRuntime {
        override val isReady: Boolean = true

        override suspend fun connect(connection: Connection) = Unit

        override suspend fun generate(request: LlmRequest): LlmResponse {
            val firstMessage = request.messages.first()
            assertTrue(firstMessage is LlmMessage.User && firstMessage.content.isNotBlank())
            return responses.removeAt(0)
        }

        override fun stopGeneration() = Unit

        override fun close() = Unit
    }

    private class EchoTool : Tool<NoArgs> {
        var executions = 0

        override val name = "echo"
        override val description = "Echoes a static result"
        override val risk = ToolRisk.SAFE
        override val argsSerializer = NoArgs.serializer()

        override suspend fun executeTyped(args: NoArgs): JsonObject {
            executions += 1
            return buildJsonObject { put("ok", true) }
        }
    }

    private class ConfirmTool : Tool<NoArgs> {
        var executions = 0

        override val name = "confirm"
        override val description = "Needs confirmation"
        override val risk = ToolRisk.REQUIRES_CONFIRMATION
        override val argsSerializer = NoArgs.serializer()

        override suspend fun executeTyped(args: NoArgs): JsonObject {
            executions += 1
            return buildJsonObject { put("ok", true) }
        }
    }
}
