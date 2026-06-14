package com.artrubadur.tonemo.agent.tools

class ToolRegistry(
    tools: List<AgentTool<*>>
) {
    private val byName = tools.associateBy { it.name }

    fun get(name: String): AgentTool<*>? = byName[name]

    fun all(): List<AgentTool<*>> = byName.values.toList()
}