package com.playwright.remote.engine.serialize.serializers

import com.google.gson.*
import com.playwright.remote.core.exceptions.PlaywrightException
import java.lang.reflect.Type

class StringMapSerializer : JsonSerializer<Map<String, String>> {
    override fun serialize(
        src: Map<String, String>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if ("java.util.Map<java.lang.String, ? extends java.lang.String>" != typeOfSrc!!.typeName) {
            throw PlaywrightException("Unexpected map type: $typeOfSrc")
        }
        return mapToJsonArray(src)
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