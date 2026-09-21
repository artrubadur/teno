package com.artrubadur.teno.agent.tools.integrations.contacts

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.provider.ContactsContract
import com.artrubadur.teno.agent.tools.integrations.search.expandSearchQueries
import com.artrubadur.teno.agent.tools.integrations.search.matchesSearchQuery
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun ContentResolver.findContactIds(query: String): Set<Long> {
    val searchQueries = expandSearchQueries(listOf(query))
    val ids = linkedSetOf<Long>()

    query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
        val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
        while (cursor.moveToNext()) {
            if (searchQueries.any {
                    cursor.getString(nameColumn).orEmpty().matchesSearchQuery(it)
                }) {
                ids += cursor.getLong(idColumn)
            }
        }
    }

    query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
        ),
        "${ContactsContract.Data.MIMETYPE} IN (?, ?)",
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
        ),
        null,
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
        val mimeColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
        val valueColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1)
        while (cursor.moveToNext()) {
            val value = cursor.getString(valueColumn).orEmpty()
            val matches = searchQueries.any { searchQuery ->
                if (cursor.getString(mimeColumn) == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                    val normalizedDigits = searchQuery.filter(Char::isDigit)
                    normalizedDigits.isNotEmpty() && value.filter(Char::isDigit)
                        .contains(normalizedDigits)
                } else {
                    value.matchesSearchQuery(searchQuery)
                }
            }
            if (matches) ids += cursor.getLong(idColumn)
        }
    }

    return ids
}

internal fun ContentResolver.readContact(
    contactId: Long,
    includeEmptyLists: Boolean = true,
): JsonObject {
    var name: String? = null
    query(
        ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId),
        arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
        null,
        null,
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) name = cursor.getString(0)
    } ?: error("Contact not found")

    val phones = mutableListOf<String>()
    val emails = mutableListOf<String>()
    query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(ContactsContract.Data.MIMETYPE, ContactsContract.Data.DATA1),
        "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE} IN (?, ?)",
        arrayOf(
            contactId.toString(),
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
        ),
        null,
    )?.use { cursor ->
        val mimeColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
        val valueColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1)
        while (cursor.moveToNext()) {
            val value = cursor.getString(valueColumn)?.takeIf { it.isNotBlank() } ?: continue
            if (cursor.getString(mimeColumn) == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
                phones += value
            } else {
                emails += value
            }
        }
    }

    return buildJsonObject {
        put("contact_id", contactId)
        put("name", name.orEmpty())
        if (includeEmptyLists || phones.isNotEmpty()) {
            put("phones", JsonArray(phones.map(::JsonPrimitive)))
        }
        if (includeEmptyLists || emails.isNotEmpty()) {
            put("emails", JsonArray(emails.map(::JsonPrimitive)))
        }
    }
}

internal fun ContentResolver.insertContact(
    name: String,
    phone: String?,
    email: String?,
): Long {
    val rawContactId = ContentUris.parseId(
        insert(
            ContactsContract.RawContacts.CONTENT_URI,
            ContentValues().apply {
                putNull(ContactsContract.RawContacts.ACCOUNT_TYPE)
                putNull(ContactsContract.RawContacts.ACCOUNT_NAME)
            },
        ) ?: error("Failed to create contact")
    )

    insertData(rawContactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {
        put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
    }
    phone?.let {
        insertData(rawContactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, it)
        }
    }
    email?.let {
        insertData(rawContactId, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE) {
            put(ContactsContract.CommonDataKinds.Email.ADDRESS, it)
        }
    }

    return queryContactId(rawContactId) ?: rawContactId
}

internal fun ContentResolver.updateContact(
    contactId: Long,
    name: String?,
    phone: String?,
    email: String?,
) {
    val rawContactId = queryRawContactId(contactId) ?: error("Contact not found")
    name?.let {
        updateData(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE) {
            put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, it)
        }
    }
    phone?.let {
        updateOrInsertData(
            rawContactId,
            contactId,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        ) {
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, it)
        }
    }
    email?.let {
        updateOrInsertData(
            rawContactId,
            contactId,
            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
        ) {
            put(ContactsContract.CommonDataKinds.Email.ADDRESS, it)
        }
    }
}

private fun ContentResolver.insertData(
    rawContactId: Long,
    mimeType: String,
    values: ContentValues.() -> Unit,
) {
    insert(ContactsContract.Data.CONTENT_URI, ContentValues().apply {
        put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
        put(ContactsContract.Data.MIMETYPE, mimeType)
        values()
    }) ?: error("Failed to write contact")
}

private fun ContentResolver.updateOrInsertData(
    rawContactId: Long,
    contactId: Long,
    mimeType: String,
    values: ContentValues.() -> Unit,
) {
    val dataId = queryDataId(contactId, mimeType)
    if (dataId == null) {
        insertData(rawContactId, mimeType, values)
    } else {
        update(
            ContactsContract.Data.CONTENT_URI,
            ContentValues().apply(values),
            "${ContactsContract.Data._ID}=?",
            arrayOf(dataId.toString()),
        )
    }
}

private fun ContentResolver.updateData(
    contactId: Long,
    mimeType: String,
    values: ContentValues.() -> Unit,
) {
    val dataId = queryDataId(contactId, mimeType) ?: error("Contact data not found")
    update(
        ContactsContract.Data.CONTENT_URI,
        ContentValues().apply(values),
        "${ContactsContract.Data._ID}=?",
        arrayOf(dataId.toString()),
    )
}

private fun ContentResolver.queryContactId(rawContactId: Long): Long? =
    queryRawContactId(rawContactId)

private fun ContentResolver.queryRawContactId(contactId: Long): Long? {
    query(
        ContactsContract.RawContacts.CONTENT_URI,
        arrayOf(ContactsContract.RawContacts._ID),
        "${ContactsContract.RawContacts.CONTACT_ID}=? OR ${ContactsContract.RawContacts._ID}=?",
        arrayOf(contactId.toString(), contactId.toString()),
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) return cursor.getLong(0)
    }
    return null
}

private fun ContentResolver.queryDataId(contactId: Long, mimeType: String): Long? {
    query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(ContactsContract.Data._ID),
        "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
        arrayOf(contactId.toString(), mimeType),
        null,
    )?.use { cursor ->
        if (cursor.moveToFirst()) return cursor.getLong(0)
    }
    return null
}


