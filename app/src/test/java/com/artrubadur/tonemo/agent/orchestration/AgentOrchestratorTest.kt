package com.artrubadur.tonemo.agent.orchestration

import com.artrubadur.tonemo.agent.policy.ConfirmationManager
import com.artrubadur.tonemo.agent.policy.SafetyPolicy
import com.artrubadur.tonemo.agent.tools.NoToolArgs
import com.artrubadur.tonemo.agent.tools.Tool
import com.artrubadur.tonemo.agent.tools.ToolBroker
import com.artrubadur.tonemo.agent.tools.ToolCall
import com.artrubadur.tonemo.agent.tools.ToolRegistry
import com.artrubadur.tonemo.agent.tools.ToolRisk
import com.artrubadur.tonemo.agent.tools.toJsonSchema
import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.runtime.llm.LlmRequest
import com.artrubadur.tonemo.connection.runtime.llm.LlmResponse
import com.artrubadur.tonemo.connection.runtime.llm.LlmRuntime
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentOrchestratorTest {

    @Test
    fun `runs tool loop until final answer`() = runBlocking {
        val runtime = FakeLlmRuntime(
            responses = mutableListOf(
                LlmResponse.ToolCalls(
                    listOf(ToolCall(tool = "echo", arguments = mapOf()))
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
                    listOf(ToolCall(tool = "confirm", arguments = mapOf()))
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
                    listOf(ToolCall(tool = "confirm", arguments = mapOf()))
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
        override val isLoaded: Boolean = true

        override suspend fun connect(connection: Connection) = Unit

        override suspend fun generate(request: LlmRequest): LlmResponse {
            assertTrue(request.userRequest.isNotBlank())
            return responses.removeAt(0)
        }

        override fun stopGeneration() = Unit

        override fun close() = Unit
    }

    private class EchoTool : Tool<NoToolArgs> {
        var executions = 0

        override val name = "echo"
        override val description = "Echoes a static result"
        override val risk = ToolRisk.SAFE
        override val argsSerializer = NoToolArgs.serializer()
        override val argsSchema = argsSerializer.toJsonSchema()

        override suspend fun executeTyped(args: NoToolArgs): Map<String, Boolean> {
            executions += 1
            return mapOf("ok" to true)
        }
    }

    private class ConfirmTool : Tool<NoToolArgs> {
        var executions = 0

        override val name = "confirm"
        override val description = "Needs confirmation"
        override val risk = ToolRisk.REQUIRES_CONFIRMATION
        override val argsSerializer = NoToolArgs.serializer()
        override val argsSchema = argsSerializer.toJsonSchema()

        override suspend fun executeTyped(args: NoToolArgs): Map<String, Boolean> {
            executions += 1
            return mapOf("ok" to true)
        }
    }
}
