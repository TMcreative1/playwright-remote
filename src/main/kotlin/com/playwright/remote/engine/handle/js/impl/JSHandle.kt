package com.playwright.remote.engine.handle.js.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.domain.serialize.SerializedError
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.serializer.Serialization.Companion.deserialize
import com.playwright.remote.engine.serializer.Serialization.Companion.serializeArgument

class JSHandle(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IJSHandle {
    private val preview: String = initializer["preview"].asString

    override fun asElement(): IElementHandle? = null

    override fun dispose() {
        sendMessage("dispose")
    }

    override fun evaluate(expression: String, arg: Any?): Any {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.addProperty("world", "main")
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpression", params)
        val value = fromJson(json.asJsonObject["value"], SerializedError.SerializedValue::class.java)
        return deserialize(value)
    }

    override fun evaluateHandle(expression: String, arg: Any?): IJSHandle {
        TODO("Not yet implemented")
    }

    override fun getProperties(): Map<String, IJSHandle> {
        TODO("Not yet implemented")
    }

    override fun getProperty(propertyName: String): IJSHandle {
        TODO("Not yet implemented")
    }

    override fun jsonValue(): Any {
        TODO("Not yet implemented")
    }
}