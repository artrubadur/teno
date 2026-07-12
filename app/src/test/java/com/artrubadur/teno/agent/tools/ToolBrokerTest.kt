package com.artrubadur.teno.agent.tools

import com.artrubadur.teno.agent.orchestration.AgentSession
import com.artrubadur.teno.agent.policy.SafetyPolicy
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolBrokerTest {

    @Test
    fun `executes tool without arguments when arguments are absent`() = runBlocking {
        val broker = ToolBroker(
            registry = ToolRegistry(listOf(EchoTool())),
            safetyPolicy = SafetyPolicy()
        )

        val result = broker.execute(
            call = ToolCall(tool = "echo", id = "id", arguments = buildJsonObject { }),
            session = testSession()
        )

        assertTrue(result is BrokerResult.Executed)
        val executed = result as BrokerResult.Executed
        assertEquals("echo", executed.call.tool)
        assertTrue((executed.result.result as Map<*, *>).containsKey("ok"))
    }

    @Test
    fun `requires confirmation before executing risky tool`() = runBlocking {
        val tool = ConfirmTool()
        val broker = ToolBroker(
            registry = ToolRegistry(listOf(tool)),
            safetyPolicy = SafetyPolicy()
        )

        val pending = broker.execute(
            call = ToolCall(tool = tool.name, id = "id", arguments = buildJsonObject { }),
            session = testSession()
        )

        assertTrue(pending is BrokerResult.NeedsConfirmation)
        assertEquals(0, tool.executions)
        val confirmation = pending as BrokerResult.NeedsConfirmation
        assertEquals(tool.name, confirmation.call.tool)
        assertTrue(confirmation.call.arguments.isEmpty())
    }

    private fun testSession() = AgentSession(
        id = "test-session",
        userRequest = "What time is it?",
        tools = emptyList()
    )

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
