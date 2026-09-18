package com.artrubadur.teno.agent.tools.integrations.sms

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import com.artrubadur.teno.agent.tools.integrations.search.matchesSearchQuery
import com.artrubadur.teno.agent.tools.integrations.time.formatTimestamp
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun Context.sendSms(phoneNumber: String, message: String): JsonObject {
    checkNotNull(getSystemService(SmsManager::class.java)) {
        "SmsManager is not available"
    }.sendTextMessage(
        phoneNumber,
        null,
        message,
        null,
        null,
    )
    return buildJsonObject {
        put("ok", true)
    }
}

internal fun Context.searchSms(queries: List<String>, limit: Int): JsonArray {
    val contactNames = mutableMapOf<String, String?>()
    val messages = if (queries.isEmpty()) {
        querySms(null, limit, contactNames)
    } else {
        queries.flatMap { query ->
            querySms(
                query,
                limit,
                contactNames,
            )
        }
    }

    return JsonArray(mergeSmsRecords(messages, limit).map { it.value })
}

internal data class SmsSearchQuery(
    val selection: String,
    val args: List<String>,
)

internal fun buildSmsSearchQuery(
    query: String,
    contactNumbers: List<String>,
): SmsSearchQuery {
    val selectionParts = mutableListOf(
        "${Telephony.Sms.ADDRESS} LIKE ?",
        "${Telephony.Sms.BODY} LIKE ?",
    )
    val args = mutableListOf("%$query%", "%$query%")
    if (contactNumbers.isNotEmpty()) {
        selectionParts += "${Telephony.Sms.ADDRESS} IN (${contactNumbers.joinToString(",") { "?" }})"
        args += contactNumbers
    }
    return SmsSearchQuery(selectionParts.joinToString(" OR "), args)
}

private fun Context.querySms(
    searchQuery: String?,
    limit: Int,
    contactNames: MutableMap<String, String?>,
): List<SmsRecord> {
    val messages = mutableListOf<SmsRecord>()
    contentResolver.query(
        Telephony.Sms.CONTENT_URI,
        arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE,
            Telephony.Sms.THREAD_ID,
        ),
        null,
        null,
        "${Telephony.Sms.DATE} DESC",
    )?.use { cursor ->
        val addressColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
        val bodyColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
        val dateColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
        val typeColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)
        val threadColumn = cursor.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID)
        val idColumn = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)

        while (cursor.moveToNext() && messages.size < limit) {
            val id = cursor.getLong(idColumn)
            val phoneNumber = cursor.getString(addressColumn).orEmpty()
            val timestamp = cursor.getLong(dateColumn)
            val type = smsType(cursor.getInt(typeColumn))
            val contact = contactName(phoneNumber, contactNames)
                ?: phoneNumber.ifBlank { "unknown" }
            val body = cursor.getString(bodyColumn).orEmpty()
            if (searchQuery != null && !phoneNumber.matchesSearchQuery(searchQuery) &&
                !body.matchesSearchQuery(searchQuery) &&
                !contact.matchesSearchQuery(searchQuery)
            ) {
                continue
            }
            val message = buildJsonObject {
                put("phone_number", phoneNumber)
                if (type == "sent") {
                    put("sender_name", "self")
                    put("recipient_name", contact)
                } else {
                    put("sender_name", contact)
                    put("recipient_name", "self")
                }
                put(
                    "message",
                    body.replace(Regex("[\\r\\n]+"), " ")
                )
                put("time", formatTimestamp(timestamp))
                put("thread_id", cursor.getLong(threadColumn))
            }
            messages += SmsRecord(id, timestamp, message)
        }
    }
    return messages
}

internal data class SmsRecord(
    val id: Long,
    val timestamp: Long,
    val value: JsonObject,
)

internal fun mergeSmsRecords(records: List<SmsRecord>, limit: Int): List<SmsRecord> =
    records
        .distinctBy { it.id }
        .sortedByDescending { it.timestamp }
        .take(limit)
        .asReversed()

private fun Context.contactName(
    phoneNumber: String,
    cache: MutableMap<String, String?>,
): String? {
    if (phoneNumber.isBlank()) return null

    return cache.getOrPut(phoneNumber) {
        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber),
        )
        contentResolver.query(
            lookupUri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.DISPLAY_NAME)
            if (cursor.moveToFirst()) cursor.getString(nameColumn) else null
        }
    }
}

private fun smsType(type: Int): String = when (type) {
    Telephony.Sms.MESSAGE_TYPE_INBOX -> "inbox"
    Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
    Telephony.Sms.MESSAGE_TYPE_DRAFT -> "draft"
    Telephony.Sms.MESSAGE_TYPE_OUTBOX -> "outbox"
    Telephony.Sms.MESSAGE_TYPE_FAILED -> "failed"
    Telephony.Sms.MESSAGE_TYPE_QUEUED -> "queued"
    else -> "unknown"
}
