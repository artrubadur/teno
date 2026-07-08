package com.artrubadur.tonemo.agent.tools

import kotlinx.schema.json.JsonSchema
import kotlinx.schema.json.encodeToJsonObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonSchema.toJsonObject(): JsonObject {
    val root = encodeToJsonObject()
    val defs = root["\$defs"]?.jsonObject.orEmpty()

    fun inline(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> {
                val ref = element["\$ref"]
                    ?.jsonPrimitive
                    ?.content

                if (ref != null && ref.startsWith("#/\$defs/")) {
                    val key = ref.removePrefix("#/\$defs/")

                    val definition = defs[key]
                        ?: error("Missing schema definition: $key")

                    inline(definition)
                } else {
                    val fields = element
                        .filterKeys { key ->
                            key != "\$schema" &&
                                    key != "\$id" &&
                                    key != "\$defs"
                        }
                        .mapValues { (_, value) ->
                            inline(value)
                        }
                        .toMutableMap()

                    val isObject =
                        fields["type"]?.jsonPrimitive?.contentOrNull == "object"


                    if (isObject && "properties" !in fields) {
                        fields["properties"] = buildJsonObject {}
                    }

                    JsonObject(fields)
                }
            }

            is JsonArray -> {
                JsonArray(element.map(::inline))
            }

            else -> {
                element
            }
        }
    }

    return inline(root).jsonObject
}

fun JsonSchema.toJsonString(): String {
    return Json.encodeToString(
        JsonObject.serializer(),
        toJsonObject()
    )
}

@Serializable
data object NoArgs