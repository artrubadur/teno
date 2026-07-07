package com.artrubadur.tonemo.agent.tools

import com.artrubadur.tonemo.agent.orchestration.AgentSession
import com.artrubadur.tonemo.agent.policy.SafetyDecision
import com.artrubadur.tonemo.agent.policy.SafetyPolicy

class ToolBroker(
    private val registry: ToolRegistry,
    private val safetyPolicy: SafetyPolicy
) {
    suspend fun execute(
        call: ToolCall,
        session: AgentSession
    ): BrokerResult {
        val tool = registry.get(call.tool)
            ?: return BrokerResult.Blocked(
                call = call,
                result = ToolResult(
                    toolCallId = call.id,
                    tool = call.tool,
                    result = mapOf("message" to "Unknown tool"),
                )
            )

        // TODO(Blocked("Tool not allowed for this connection"))
        // TODO(Blocked("Tool not allowed in current session"))

        when (val decision = safetyPolicy.check(tool, session)) {
            is SafetyDecision.Block -> return BrokerResult.Blocked(
                call = call,
                result = ToolResult(
                    toolCallId = call.id,
                    tool = call.tool,
                    result = mapOf("message" to "Blocked: ${decision.reason}"),
                )
            )

            is SafetyDecision.RequireConfirmation -> {
                return BrokerResult.NeedsConfirmation(
                    call = call,
                    title = decision.title,
                    description = decision.description
                )
            }

            is SafetyDecision.Allow -> Unit
        }

        return executeTool(tool, call)
    }

    suspend fun executeApproved(call: ToolCall): BrokerResult {
        val tool = registry.get(call.tool)
            ?: return BrokerResult.Blocked(
                call = call,
                result = ToolResult(
                    toolCallId = call.id,
                    tool = call.tool,
                    result = mapOf("message" to "Unknown tool"),
                )
            )

        return executeTool(tool, call)
    }

    private suspend fun executeTool(
        tool: Tool<*>,
        call: ToolCall,
    ): BrokerResult {
        val result = try {
            tool.execute(call.arguments)
        } catch (e: Exception) {
            return BrokerResult.Failed(
                call = call,
                result = ToolResult(
                    toolCallId = call.id,
                    tool = call.tool,
                    result = mapOf("message" to "Failed: ${e.message ?: ""}"),
                )
            )
        }

        return BrokerResult.Executed(
            call = call,
            result = ToolResult(
                toolCallId = call.id,
                tool = call.tool,
                result = result,
            )
        )
    }

    fun listToolSpecs(): List<ToolSpec> {
        return registry.all().map { it.toSpec() }
    }
}

sealed interface BrokerResult {
    data class Executed(
        val call: ToolCall,
        val result: ToolResult
    ) : BrokerResult

    data class Blocked(
        val call: ToolCall,
        val result: ToolResult
    ) : BrokerResult

    data class Failed(
        val call: ToolCall,
        val result: ToolResult
    ) : BrokerResult

    data class NeedsConfirmation(
        val call: ToolCall,
        val title: String,
        val description: String
    ) : BrokerResult
}
