package com.playwright.remote.engine.serialize.serializers

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.playwright.remote.core.exceptions.PlaywrightException
import java.lang.reflect.Type

class FirefoxUserPrefsSerializer : JsonSerializer<Map<StringMapSerializer, Any>> {
    override fun serialize(
        src: Map<StringMapSerializer, Any>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if ("java.util.Map<java.lang.String, ?>" != typeOfSrc!!.typeName) {
            throw PlaywrightException("Unexpected map type: $typeOfSrc")
        }
        return context!!.serialize(src, Map::class.java)
    }
}