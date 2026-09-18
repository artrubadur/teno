package com.artrubadur.teno.agent.tools.integrations.search

internal fun String.matchesSearchQuery(query: String): Boolean =
    contains(query, ignoreCase = true)

internal fun expandSearchQueries(queries: List<String>): List<String> {
    val fullQueries = queries
        .map(String::trim)
        .filter(String::isNotBlank)
        .distinct()
    val fragments = fullQueries.flatMap { it.split(Regex("\\s+")) }
        .filter(String::isNotBlank)
    return (fullQueries + fragments).distinct()
}
