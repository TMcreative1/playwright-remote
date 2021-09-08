package com.playwright.remote.engine.serialize.serializers

import com.google.gson.*
import com.playwright.remote.core.exceptions.PlaywrightException
import java.lang.reflect.Type

class MapSerializer : JsonSerializer<MutableMap<String, Any>> {
    @Suppress("UNCHECKED_CAST")
    override fun serialize(
        src: MutableMap<String, Any>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return when (typeOfSrc!!.typeName) {
            "java.util.Map<java.lang.String, ?>" -> {
                context!!.serialize(src, Map::class.java)
            }
            "java.util.Map<java.lang.String, java.lang.String>" -> {
                mapToJsonArray(src as Map<String, String>)
            }
            else -> throw PlaywrightException("Unexpected map type: $typeOfSrc")
        }

    }

    private fun mapToJsonArray(map: Map<String, String>?): JsonArray {
        val array = JsonArray()
        map?.entries?.forEach {
            val item = JsonObject()
            item.addProperty("name", it.key)
            item.addProperty("value", it.value)
            array.add(item)
        }
        return array
    }
}