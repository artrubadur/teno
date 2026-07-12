package com.artrubadur.teno.agent.tools

import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator
import kotlinx.schema.json.JsonSchema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolArgsSchemaTest {

    @Test
    fun `generates object schema with properties`() {
        val schema = TestArgs.serializer().toSchema().toJsonObject()

        assertEquals(JsonPrimitive("object"), schema["type"])

        val properties = schema["properties"]!!.jsonObject
        assertEquals(JsonPrimitive("string"), properties["message"]!!.jsonObject["type"])
        assertEquals(JsonPrimitive("integer"), properties["count"]!!.jsonObject["type"])
        assertEquals(JsonPrimitive("boolean"), properties["enabled"]!!.jsonObject["type"])
        assertEquals(JsonPrimitive(false), schema["additionalProperties"])
    }

    @Test
    fun `marks only fields without defaults as required`() {
        val schema = TestArgs.serializer().toSchema().toJsonObject()

        assertEquals(
            JsonArray(listOf(JsonPrimitive("message"), JsonPrimitive("enabled"))),
            schema["required"]!!.jsonArray
        )
    }

    @Test
    fun `generates enum schema using serial names`() {
        val schema = TestArgs.serializer().toSchema().toJsonObject()
        val levelSchema = schema["properties"]!!.jsonObject["level"]!!.jsonObject

        assertEquals(JsonPrimitive("string"), levelSchema["type"])
        assertEquals(
            JsonArray(
                listOf(
                    JsonPrimitive("debug"),
                    JsonPrimitive("info"),
                    JsonPrimitive("warning"),
                    JsonPrimitive("error")
                )
            ),
            levelSchema["enum"]!!.jsonArray
        )
    }

    @Test
    fun `omits required for object with only optional fields`() {
        val schema = OptionalArgs.serializer().toSchema().toJsonObject()

        assertFalse(schema.containsKey("required"))
    }

    @Test
    fun `generates nested object schema`() {
        val schema = NestedArgs.serializer().toSchema().toJsonObject()
        val childSchema = schema["properties"]!!.jsonObject["child"]!!.jsonObject

        assertEquals(JsonPrimitive("object"), childSchema["type"])
        assertTrue(childSchema["properties"]!!.jsonObject.containsKey("message"))
    }

    @Test
    fun `generates empty object schema when no arguments are needed`() {
        val schema = NoArgs.serializer().toSchema().toJsonObject()

        assertEquals(JsonPrimitive("object"), schema["type"])
        assertTrue(schema["properties"]!!.jsonObject.isEmpty())
        assertEquals(JsonPrimitive(false), schema["additionalProperties"])
        assertFalse(schema.containsKey("required"))
    }

    private fun <T> kotlinx.serialization.KSerializer<T>.toSchema(): JsonSchema =
        SerializationClassJsonSchemaGenerator.Default
            .generateSchema(this.descriptor)

    @Serializable
    private data class TestArgs(
        val message: String,
        val count: Int = 1,
        val enabled: Boolean,
        val level: TestLevel = TestLevel.DEBUG
    )

    @Serializable
    private data class OptionalArgs(
        val message: String = "",
        val count: Int = 0
    )

    @Serializable
    private data class NestedArgs(
        val child: TestArgs
    )

    @Serializable
    private class NoArgs

    @Serializable
    private enum class TestLevel {
        @SerialName("debug")
        DEBUG,

        @SerialName("info")
        INFO,

        @SerialName("warning")
        WARNING,

        @SerialName("error")
        ERROR
    }
}
