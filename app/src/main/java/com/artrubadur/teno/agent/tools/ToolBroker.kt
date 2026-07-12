package com.artrubadur.teno.agent.tools

import com.artrubadur.teno.agent.orchestration.AgentSession
import com.artrubadur.teno.agent.policy.SafetyDecision
import com.artrubadur.teno.agent.policy.SafetyPolicy
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ToolBroker(
    private val registry: ToolRegistry,
    private val safetyPolicy: SafetyPolicy,
    private val toolManager: ToolManager,
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
                    result = buildJsonObject {
                        put("message", "Unknown tool")
                    }
                )
            )

        if (!toolManager.isEnabled(tool)) {
            return disabledToolResult(call)
        }

        // TODO(Blocked("Tool not allowed for this connection"))
        // TODO(Blocked("Tool not allowed in current session"))

        when (val decision = safetyPolicy.check(tool, session)) {
            is SafetyDecision.Block -> return BrokerResult.Blocked(
                call = call,
                result = ToolResult(
                    toolCallId = call.id,
                    tool = call.tool,
                    result = buildJsonObject {
                        put("message", "Blocked: ${decision.reason}")
                    }
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
                    result = buildJsonObject {
                        put("message", "Unknown tool")
                    }
                )
            )

        if (!toolManager.isEnabled(tool)) {
            return disabledToolResult(call)
        }

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
                    result = buildJsonObject {
                        put("message", "Failed: ${e.message ?: ""}")
                    }
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

    suspend fun listToolSpecs(): List<ToolSpec> {
        return toolManager.enabledSpecs()
    }

    private fun disabledToolResult(call: ToolCall): BrokerResult.Blocked {
        return BrokerResult.Blocked(
            call = call,
            result = ToolResult(
                toolCallId = call.id,
                tool = call.tool,
                result = buildJsonObject {
                    put("message", "Tool is disabled")
                }
            )
        )
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
