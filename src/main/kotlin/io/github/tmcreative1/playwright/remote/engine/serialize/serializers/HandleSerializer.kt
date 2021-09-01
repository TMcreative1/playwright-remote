package io.github.tmcreative1.playwright.remote.engine.serialize.serializers

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import io.github.tmcreative1.playwright.remote.engine.handle.js.impl.JSHandle
import java.lang.reflect.Type

class HandleSerializer : JsonSerializer<JSHandle> {

    override fun serialize(src: JSHandle?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val json = JsonObject()
        json.addProperty("guid", src!!.guid)
        return json
    }
}