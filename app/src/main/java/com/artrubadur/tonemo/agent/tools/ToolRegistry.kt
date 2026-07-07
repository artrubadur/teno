package com.artrubadur.tonemo.agent.tools

class ToolRegistry(
    tools: List<Tool<*>>
) {
    private val byName = tools.associateBy { it.name }

    fun get(name: String): Tool<*>? = byName[name]

    fun all(): List<Tool<*>> = byName.values.toList()
}
