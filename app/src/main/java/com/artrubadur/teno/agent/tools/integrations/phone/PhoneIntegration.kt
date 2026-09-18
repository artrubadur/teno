package com.artrubadur.teno.agent.tools.integrations.phone

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import com.artrubadur.teno.agent.tools.integrations.search.expandSearchQueries
import com.artrubadur.teno.agent.tools.integrations.search.matchesSearchQuery
import com.artrubadur.teno.agent.tools.integrations.time.formatTimestamp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun Context.callPhoneNumber(phoneNumber: String): JsonObject {
    val intent = Intent(
        Intent.ACTION_CALL,
        Uri.fromParts("tel", phoneNumber, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
    return buildJsonObject {
        put("ok", true)
        put("phone_number", phoneNumber)
    }
}

internal fun Context.getCallHistory(queries: List<String>, limit: Int): JsonArray {
    val searchQueries = expandSearchQueries(queries)
    val calls = if (searchQueries.isEmpty()) {
        queryCallHistory(null, limit)
    } else {
        searchQueries.flatMap { query ->
            queryCallHistory(query, limit)
        }
    }

    return JsonArray(
        calls
            .distinctBy { it.id }
            .sortedByDescending { it.timestamp }
            .take(limit)
            .asReversed()
            .map { it.value }
    )
}

private fun Context.queryCallHistory(
    searchQuery: String?,
    limit: Int,
): List<CallRecord> {
    val calls = mutableListOf<CallRecord>()
    contentResolver.query(
        CallLog.Calls.CONTENT_URI,
        arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
        ),
        null,
        null,
        "${CallLog.Calls.DATE} DESC",
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
        val numberColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
        val nameColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
        val typeColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
        val dateColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
        val durationColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)

        while (cursor.moveToNext() && calls.size < limit) {
            val timestamp = cursor.getLong(dateColumn)
            val phoneNumber = cursor.getString(numberColumn).orEmpty()
            val name = cursor.getString(nameColumn).orEmpty()
            if (searchQuery != null && !phoneNumber.matchesSearchQuery(searchQuery) &&
                !name.matchesSearchQuery(searchQuery)
            ) {
                continue
            }
            val call = buildJsonObject {
                put("phone_number", phoneNumber)
                name.takeIf { it.isNotBlank() }?.let { put("name", it) }
                put("type", callType(cursor.getInt(typeColumn)))
                put("time", formatTimestamp(timestamp))
                put("duration_seconds", cursor.getLong(durationColumn))
            }
            calls += CallRecord(cursor.getLong(idColumn), timestamp, call)
        }
    }
    return calls
}

private data class CallRecord(
    val id: Long,
    val timestamp: Long,
    val value: JsonObject,
)

private fun callType(type: Int): String = when (type) {
    CallLog.Calls.INCOMING_TYPE -> "incoming"
    CallLog.Calls.OUTGOING_TYPE -> "outgoing"
    CallLog.Calls.MISSED_TYPE -> "missed"
    CallLog.Calls.REJECTED_TYPE -> "rejected"
    CallLog.Calls.BLOCKED_TYPE -> "blocked"
    CallLog.Calls.VOICEMAIL_TYPE -> "voicemail"
    else -> "unknown"
}
