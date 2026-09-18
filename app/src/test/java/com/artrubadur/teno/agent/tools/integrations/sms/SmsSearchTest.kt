package com.artrubadur.teno.agent.tools.integrations.sms

import android.provider.Telephony
import kotlinx.serialization.json.buildJsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsSearchTest {

    @Test
    fun searchMatchesAddressOrMessageBody() {
        val query = buildSmsSearchQuery("John", emptyList())

        assertEquals(
            "${Telephony.Sms.ADDRESS} LIKE ? OR ${Telephony.Sms.BODY} LIKE ?",
            query.selection,
        )
        assertEquals(listOf("%John%", "%John%"), query.args)
    }

    @Test
    fun contactMatchesAreAlsoJoinedWithOr() {
        val query = buildSmsSearchQuery("John", listOf("+79998887766", "888"))

        assertTrue(query.selection.contains(" OR "))
        assertEquals(
            listOf("%John%", "%John%", "+79998887766", "888"),
            query.args,
        )
    }

    @Test
    fun mergeKeepsLatestLimitAcrossQueryResults() {
        val records = listOf(
            record(id = 1, timestamp = 100),
            record(id = 2, timestamp = 300),
            record(id = 3, timestamp = 200),
            record(id = 2, timestamp = 300),
        )

        val result = mergeSmsRecords(records, limit = 3)

        assertEquals(listOf(1L, 3L, 2L), result.map { it.id })
    }

    private fun record(id: Long, timestamp: Long) = SmsRecord(
        id = id,
        timestamp = timestamp,
        value = buildJsonObject { },
    )
}
