package com.artrubadur.tonemo.agent.orchestration

import com.artrubadur.tonemo.agent.tools.ToolDescriptor

class AgentPromptBuilder {
    fun build(
        userRequest: String,
        availableTools: List<ToolDescriptor>,
        toolResults: List<ToolResultEntry>
    ): String {
        return buildString {
            appendLine("User request:")
            appendLine(userRequest)
            appendLine()
            appendLine("Available tools:")
            if (availableTools.isEmpty()) {
                appendLine("- none")
            } else {
                availableTools.forEach { tool ->
                    appendLine("- ${tool.name}: ${tool.description}")
                    appendLine("  risk: ${tool.risk}")
                    appendLine("  argsSchema: ${tool.argsSchema}")
                }
            }
            appendLine()
            appendLine("Tool results:")
            if (toolResults.isEmpty()) {
                appendLine("- none")
            } else {
                toolResults.forEach { result ->
                    appendLine("- ${result.tool}: ${result.resultJson}")
                }
            }
            appendLine()
            appendLine("Return exactly one JSON object that matches the task.")
        }
    }
}
