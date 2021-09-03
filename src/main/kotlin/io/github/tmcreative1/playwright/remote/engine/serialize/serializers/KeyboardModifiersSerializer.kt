package com.playwright.remote.engine.serialize.serializers

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.playwright.remote.core.enums.KeyboardModifier
import com.playwright.remote.core.enums.KeyboardModifier.*
import java.lang.reflect.Type

class KeyboardModifiersSerializer : JsonSerializer<MutableList<KeyboardModifier>> {


    override fun serialize(
        src: MutableList<KeyboardModifier>?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
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