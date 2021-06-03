package com.playwright.remote.engine.serialize.serializers

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.nio.file.Path

class PathSerializer : JsonSerializer<Path> {
    override fun serialize(src: Path?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src.toString())
    }
}