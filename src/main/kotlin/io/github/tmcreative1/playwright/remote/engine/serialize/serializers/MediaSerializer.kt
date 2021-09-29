package io.github.tmcreative1.playwright.remote.engine.serialize.serializers

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.github.tmcreative1.playwright.remote.core.enums.Media
import java.lang.reflect.Type

class MediaSerializer : JsonSerializer<Media> {
    override fun serialize(src: Media?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
        JsonPrimitive(src.toString().lowercase())
}