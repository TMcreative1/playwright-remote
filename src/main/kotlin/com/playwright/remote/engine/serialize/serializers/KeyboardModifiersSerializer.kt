package com.playwright.remote.engine.serialize.serializers

import com.google.gson.JsonArray
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.playwright.remote.core.enums.KeyboardModifier
import com.playwright.remote.core.enums.KeyboardModifier.*
import java.lang.reflect.Type

class KeyboardModifiersSerializer : JsonSerializer<List<KeyboardModifier>> {

    override fun serialize(
        src: List<KeyboardModifier>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonArray {
        val result = JsonArray()
        if (src!!.contains(ALT)) {
            result.add("Alt")
        }
        if (src.contains(CONTROL)) {
            result.add("Control")
        }
        if (src.contains(META)) {
            result.add("Meta")
        }
        if (src.contains(SHIFT)) {
            result.add("Shift")
        }
        return result
    }
}