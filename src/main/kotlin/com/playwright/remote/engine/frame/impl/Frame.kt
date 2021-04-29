package com.playwright.remote.engine.frame.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.domain.serialize.SerializedError.SerializedValue
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.element.ClickOptions
import com.playwright.remote.engine.options.element.DoubleClickOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.serializer.Serialization.Companion.deserialize
import com.playwright.remote.engine.serializer.Serialization.Companion.serializeArgument
import okio.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.readAllBytes

class Frame(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IFrame {
    private val name: String = initializer["name"].asString
    private val url: String = initializer["url"].asString
    private var parentFrame: IFrame? = null
    private val childFrames = linkedSetOf<IFrame>()
    private val loadStates = hashSetOf<LoadState>()
    private var page: IPage? = null

    init {
        if (initializer.has("parentFrame")) {
            parentFrame = messageProcessor.getExistingObject(initializer["parentFrame"].asJsonObject["guid"].asString)
            (parentFrame as Frame).childFrames.add(this)
        }
        for (item in initializer["loadStates"].asJsonArray) {
            loadStates.add(LoadState.valueOf(item.asString.toUpperCase()))
        }
    }

    override fun page(): IPage? {
        return page
    }

    override fun navigate(url: String, options: NavigateOptions): IResponse? {
        val params = Gson().toJsonTree(options).asJsonObject
        params.addProperty("url", url)
        val result = sendMessage("goto", params)
        val jsonResponse = result.asJsonObject["response"].asJsonObject ?: return null
        return messageProcessor.getExistingObject(jsonResponse["guid"].asString)
    }

    fun addScriptTag(options: AddScriptTagOptions?): IElementHandle {
        val opt = options ?: AddScriptTagOptions {}
        val jsonOptions = Gson().toJsonTree(opt).asJsonObject
        if (opt.path != null) {
            jsonOptions.remove("path")
            val encoded: ByteArray
            try {
                encoded = readAllBytes(opt.path!!)
            } catch (e: IOException) {
                throw PlaywrightException("Failed to read from file", e)
            }
            var content = String(encoded, UTF_8)
            content += "//$ sourceURL= ${opt.path.toString().replace("\n", "")}"
            jsonOptions.addProperty("content", content)
        }
        val json = sendMessage("addScriptTag", jsonOptions)
        return messageProcessor.getExistingObject(json.asJsonObject["element"].asJsonObject["guid"].asString)
    }

    fun addStyleTag(options: AddStyleTagOptions?): IElementHandle {
        val opt = options ?: AddStyleTagOptions {}
        val jsonOptions = Gson().toJsonTree(opt).asJsonObject
        if (opt.path != null) {
            jsonOptions.remove("path")
            val encoded: ByteArray
            try {
                encoded = readAllBytes(opt.path)
            } catch (e: IOException) {
                throw PlaywrightException("Failed to read from", e)
            }
            var content = String(encoded, UTF_8)
            content += "/*# sourceURL=${opt.path.toString().replace("\n", "")}*/"
            jsonOptions.addProperty("content", content)
        }
        val json = sendMessage("addStyleTag", jsonOptions)
        return messageProcessor.getExistingObject(json.asJsonObject["element"].asJsonObject["guid"].asString)
    }

    fun check(selector: String, options: CheckOptions?) {
        val opt = options ?: CheckOptions {}
        val params = Gson().toJsonTree(opt).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("check", params)
    }

    fun click(selector: String, options: ClickOptions?) {
        val opt = options ?: ClickOptions {}
        val params = Gson().toJsonTree(opt).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("click", params)
    }

    fun content(): String {
        return sendMessage("content").asJsonObject["value"].asString
    }

    fun doubleClick(selector: String, options: DoubleClickOptions?) {
        val params = Gson().toJsonTree(options ?: DoubleClickOptions {}).asJsonObject
        params.addProperty("selector", selector)
        sendMessage("dblclick", params)
    }

    fun dispatchEvent(selector: String, type: String, eventInit: Any?, options: DispatchEventOptions?) {
        val params = Gson().toJsonTree(options ?: DispatchEventOptions {}).asJsonObject
        params.addProperty("selector", selector)
        params.addProperty("type", type)
        params.add("eventInit", Gson().toJsonTree(serializeArgument(eventInit)))
        sendMessage("dispatchEvent", params)
    }

    fun evalOnSelector(selector: String, expression: String, arg: Any?): Any =
        evalOnSelector(selector, expression, arg, "evalOnSelector")

    fun evalOnSelectorAll(selector: String, expression: String, arg: Any?): Any =
        evalOnSelector(selector, expression, arg, "evalOnSelectorAll")

    private fun evalOnSelector(selector: String, expression: String, arg: Any?, method: String): Any {
        val params = JsonObject()
        params.addProperty("selector", selector)
        params.addProperty("expression", expression)
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage(method, params)
        val value = fromJson(json.asJsonObject["value"], SerializedValue::class.java)
        return deserialize(value)
    }

    fun evaluate(expression: String, arg: Any?): Any {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.addProperty("world", "main")
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpression", params)
        val value = fromJson(json.asJsonObject["value"], SerializedValue::class.java)
        return deserialize(value)
    }

    fun evaluateHandle(expression: String, arg: Any?): IJSHandle {
        val params = JsonObject()
        params.addProperty("expression", expression)
        params.addProperty("world", "main")
        params.add("arg", Gson().toJsonTree(serializeArgument(arg)))
        val json = sendMessage("evaluateExpressionHandle", params)
        return messageProcessor.getExistingObject(json.asJsonObject["handle"].asJsonObject["guid"].asString)
    }
}