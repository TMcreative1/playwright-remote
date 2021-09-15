package io.github.tmcreative1.playwright.remote.engine.serialize.serializers

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

class ToLowerCaseSerializer<E : Enum<E>> : JsonSerializer<E> {
    override fun serialize(src: E, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
        JsonPrimitive(src.toString().lowercase())
}