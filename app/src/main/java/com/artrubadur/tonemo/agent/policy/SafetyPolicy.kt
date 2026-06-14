package com.artrubadur.tonemo.agent.policy

import com.artrubadur.tonemo.agent.orchestration.AgentSession
import com.artrubadur.tonemo.agent.tools.AgentTool

class SafetyPolicy {
    fun check(
        tool: AgentTool<*>,
        args: Any,
        session: AgentSession
    ): SafetyDecision {
        return when (tool.risk) {
            com.artrubadur.tonemo.agent.tools.ToolRisk.SAFE -> SafetyDecision.Allow
            com.artrubadur.tonemo.agent.tools.ToolRisk.REQUIRES_CONFIRMATION ->
                SafetyDecision.RequireConfirmation(
                    title = "Confirm '${tool.name}' tool execution",
                    description = tool.description
                )

            com.artrubadur.tonemo.agent.tools.ToolRisk.FORBIDDEN ->
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
