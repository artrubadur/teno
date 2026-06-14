package com.artrubadur.tonemo.agent.tools

import com.artrubadur.tonemo.agent.orchestration.AgentSession
import com.artrubadur.tonemo.agent.policy.SafetyPolicy
import com.artrubadur.tonemo.agent.tools.impl.TimeTool
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolBrokerTest {

    @Test
    fun `executes tool without arguments when args json is absent`() = runBlocking {
        val broker = ToolBroker(
            registry = ToolRegistry(listOf(TimeTool())),
            safetyPolicy = SafetyPolicy()
        )

        val result = broker.dispatch(
            call = ToolCall(tool = "time", argsJson = null),
            session = testSession()
        )

        assertTrue(result is BrokerResult.Executed)
        val executed = result as BrokerResult.Executed
        assertEquals("time", executed.tool)
        assertTrue(Json.parseToJsonElement(executed.resultJson).jsonObject.containsKey("time"))
    }

    @Test
    fun `requires confirmation before executing risky tool`() = runBlocking {
        val tool = ConfirmTool()
        val broker = ToolBroker(
            registry = ToolRegistry(listOf(tool)),
            safetyPolicy = SafetyPolicy()
        )

        val pending = broker.dispatch(
            call = ToolCall(tool = tool.name, argsJson = null),
            session = testSession()
        )

        assertTrue(pending is BrokerResult.NeedsConfirmation)
        assertEquals(0, tool.executions)
        val confirmation = pending as BrokerResult.NeedsConfirmation
        assertEquals(tool.name, confirmation.tool)
        assertEquals("{}", confirmation.argsJson)
    }

    private fun testSession() = AgentSession(
        id = "test-session",
        userMessage = "What time is it?",
        availableTools = emptyList()
    )

    private class ConfirmTool : AgentTool<NoToolArgs> {
        var executions = 0

        override val name = "confirm"
        override val description = "Requires confirmation"
        override val risk = ToolRisk.REQUIRES_CONFIRMATION
        override val argsSerializer = NoToolArgs.serializer()
        override val argsSchema = argsSerializer.toJsonSchema()

        override suspend fun execute(args: NoToolArgs): ToolResult {
            executions += 1
            return ToolResult.Success("""{"executed":true}""")
        }
    }
}
