package com.artrubadur.tonemo.agent.tools

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

fun <T> KSerializer<T>.toJsonSchema(): String {
    val schema = descriptor.toSchema()
    return Json.encodeToString(JsonObject.serializer(), schema)
}

@OptIn(ExperimentalSerializationApi::class)
private fun SerialDescriptor.toSchema(): JsonObject {
    return when (kind) {
        StructureKind.CLASS -> {
            val properties = buildJsonObject {
                for (i in 0 until elementsCount) {
                    put(getElementName(i), getElementDescriptor(i).toSchema())
                }
            }

            val required = buildJsonArray {
                for (i in 0 until elementsCount) {
                    if (!isElementOptional(i)) add(getElementName(i))
                }
            }

            buildJsonObject {
                put("type", "object")
                put("properties", properties)
                put("additionalProperties", false)
                if (required.isNotEmpty()) put("required", required)
            }
        }

        SerialKind.ENUM -> buildJsonObject {
            put("type", "string")
            putJsonArray("enum") {
                for (i in 0 until elementsCount) {
                    add(getElementName(i))
                }
            }
        }

        PrimitiveKind.STRING -> buildJsonObject { put("type", "string") }
        PrimitiveKind.INT, PrimitiveKind.LONG -> buildJsonObject { put("type", "integer") }
        PrimitiveKind.FLOAT, PrimitiveKind.DOUBLE -> buildJsonObject { put("type", "number") }
        PrimitiveKind.BOOLEAN -> buildJsonObject { put("type", "boolean") }

        else -> buildJsonObject {
            put("type", "object")
        }
    }
}

@Serializable
class NoToolArgs
