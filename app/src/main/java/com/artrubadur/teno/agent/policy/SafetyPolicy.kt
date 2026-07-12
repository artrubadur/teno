package com.artrubadur.teno.agent.policy

import com.artrubadur.teno.agent.orchestration.AgentSession
import com.artrubadur.teno.agent.tools.Tool

class SafetyPolicy {
    fun check(
        tool: Tool<*>,
        session: AgentSession
    ): SafetyDecision {
        return when (tool.risk) {
            com.artrubadur.teno.agent.tools.ToolRisk.SAFE -> SafetyDecision.Allow
            com.artrubadur.teno.agent.tools.ToolRisk.REQUIRES_CONFIRMATION ->
                SafetyDecision.RequireConfirmation(
                    title = "Confirm '${tool.name}' tool execution",
                    description = tool.description
                )

            com.artrubadur.teno.agent.tools.ToolRisk.FORBIDDEN ->
                SafetyDecision.Block("Tool '${tool.name}' is forbidden")
        }
    }
}

sealed class SafetyDecision {
    data object Allow : SafetyDecision()

    data class RequireConfirmation(
        val title: String,
        val description: String
    ) : SafetyDecision()

    data class Block(
        val reason: String
    ) : SafetyDecision()
}
