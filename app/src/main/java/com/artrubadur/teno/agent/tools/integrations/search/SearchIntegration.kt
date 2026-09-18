package com.artrubadur.teno.agent.tools.integrations.search

internal fun String.matchesSearchQuery(query: String): Boolean =
    contains(query, ignoreCase = true)
