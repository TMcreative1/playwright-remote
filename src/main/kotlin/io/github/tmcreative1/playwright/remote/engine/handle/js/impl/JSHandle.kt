package io.github.tmcreative1.playwright.remote.engine.handle.js.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.fromJson
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.deserialize
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.serializeArgument
import io.github.tmcreative1.playwright.remote.domain.serialize.SerializedError
import io.github.tmcreative1.playwright.remote.engine.handle.element.api.IElementHandle
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle

open class JSHandle(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IJSHandle {
    private var preview: String = initializer["preview"].asString

    override fun asElement(): IElementHandle? = null

    override fun dispose() {
        sendMessage("dispose")
    }

    override fun evaluate(expression: String, arg: Any?): Any {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.addProperty("world", "main")
        params.add("arg", gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpression", params)
        val value = fromJson(json!!.asJsonObject["value"], SerializedError.SerializedValue::class.java)
        return deserialize(value)
    }

    override fun evaluateHandle(expression: String, arg: Any?): IJSHandle {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.addProperty("world", "main")
        params.add("arg", gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpressionHandle", params)
        return messageProcessor.getExistingObject(json!!.asJsonObject["handle"].asJsonObject["guid"].asString)
    }

    override fun getProperties(): Map<String, IJSHandle> {
        val json = sendMessage("getPropertyList")!!.asJsonObject["properties"]
        val result = hashMapOf<String, IJSHandle>()
        for (element in json.asJsonArray) {
            val item = element.asJsonObject
            val value = messageProcessor.getExistingObject<IJSHandle>(item["value"].asJsonObject["guid"].asString)
            result[item["name"].asString] = value
        }
        return result
    }

    override fun getProperty(propertyName: String): IJSHandle {
        val params = JsonObject()
        params.addProperty("name", propertyName)
        val json = sendMessage("getProperty", params)!!.asJsonObject["handle"]
        return messageProcessor.getExistingObject(json.asJsonObject["guid"].asString)
    }

    override fun jsonValue(): Any {
        val json = sendMessage("jsonValue")!!.asJsonObject
        val value = fromJson(json["value"], SerializedError.SerializedValue::class.java)
        return deserialize(value)
    }

    override fun handleEvent(event: String, params: JsonObject) {
        if (event == "previewUpdated") {
            preview = params["preview"].asString
        }
        super.handleEvent(event, params)
    }

    override fun toString(): String {
        return preview
    }
}