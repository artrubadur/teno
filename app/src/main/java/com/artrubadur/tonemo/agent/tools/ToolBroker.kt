package com.artrubadur.tonemo.agent.tools

import com.artrubadur.tonemo.agent.orchestration.AgentSession
import com.artrubadur.tonemo.agent.policy.SafetyDecision
import com.artrubadur.tonemo.agent.policy.SafetyPolicy

class ToolBroker(
    private val registry: ToolRegistry,
    private val safetyPolicy: SafetyPolicy
) {
    suspend fun dispatch(
        call: ToolCall,
        session: AgentSession
    ): BrokerResult {
        val tool = registry.get(call.tool)
            ?: return BrokerResult.Blocked(call.tool, "Unknown tool")

        // TODO(Blocked("Tool not allowed for this model"))
        // TODO(Blocked("Tool not allowed in current session"))

        val args = try {
            tool.decodeArgs(call.argsJson)
        } catch (e: Exception) {
            return BrokerResult.Blocked(tool.name, "Invalid arguments: ${e.message}")
        }

        when (val safety = safetyPolicy.check(tool, args, session)) {
            is SafetyDecision.Block -> return BrokerResult.Blocked(tool.name, safety.reason)
            is SafetyDecision.RequireConfirmation -> {
                return BrokerResult.NeedsConfirmation(
                    tool = tool.name,
                    argsJson = call.argsJson ?: "{}",
                    title = safety.title,
                    description = safety.description
                )
            }

            is SafetyDecision.Allow -> Unit
        }

        return executeTool(tool, args)
    }

    suspend fun executeApproved(call: ToolCall): BrokerResult {
        val tool = registry.get(call.tool)
            ?: return BrokerResult.Blocked(call.tool, "Unknown tool")

        val args = try {
            tool.decodeArgs(call.argsJson)
        } catch (e: Exception) {
            return BrokerResult.Blocked(tool.name, "Invalid arguments: ${e.message}")
        }

        return executeTool(tool, args)
    }

    fun listAvailableTools(): List<ToolDescriptor> {
        return registry.all().map { it.toDescriptor() }
    }

    private suspend fun executeTool(
        tool: AgentTool<*>,
        args: Any
    ): BrokerResult {
        return when (val result = tool.executeUntyped(args)) {
            is ToolResult.Success -> BrokerResult.Executed(
                tool = tool.name,
                resultJson = result.resultJson
            )

            is ToolResult.Error -> BrokerResult.Failed(tool.name, result.message)
        }
    }
}

data class ToolCall(
    val tool: String,
    val argsJson: String?
)

sealed class BrokerResult {
    data class Executed(
        val tool: String,
        val resultJson: String
    ) : BrokerResult()

    data class NeedsConfirmation(
        val tool: String,
        val argsJson: String,
        val title: String,
        val description: String
    ) : BrokerResult()

    data class Blocked(
        val tool: String,
        val reason: String
    ) : BrokerResult()

    data class Failed(
        val tool: String,
        val reason: String
    ) : BrokerResult()
}
