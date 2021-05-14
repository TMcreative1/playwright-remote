package com.playwright.remote.engine.route.response.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.request.Timing
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.request.impl.Request
import com.playwright.remote.engine.route.response.api.IResponse
import java.nio.charset.StandardCharsets
import java.util.*

class Response(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IResponse {
    private val headers = hashMapOf<String, String>()
    private val request: IRequest

    init {
        for (element in initializer["headers"].asJsonArray) {
            val item = element.asJsonObject
            headers[item["name"].asString.toLowerCase()] = item["value"].asString
        }
        request = messageProcessor.getExistingObject(initializer["request"].asJsonObject["guid"].asString)
        (request as Request).headers.clear()
        for (element in initializer["requestHeaders"].asJsonArray) {
            val item = element.asJsonObject
            request.headers[item["name"].asString.toLowerCase()] = item["value"].asString
        }
        request.timing = fromJson(initializer["timing"], Timing::class.java)
    }

    override fun body(): ByteArray {
        val json = sendMessage("body").asJsonObject
        return Base64.getDecoder().decode(json["binary"].asString)
    }

    override fun finished(): String? {
        val json = sendMessage("finished").asJsonObject
        if (json.has("error")) {
            return json["error"].asString
        }
        return null
    }

    override fun frame(): IFrame = request().frame()

    override fun headers(): Map<String, String> = headers

    override fun ok(): Boolean = status() == 0 || (status() in 200..299)

    override fun request(): IRequest = request

    override fun status(): Int = initializer["status"].asInt

    override fun statusText(): String = initializer["statusText"].asString

    override fun text(): String = String(body(), StandardCharsets.UTF_8)

    override fun url(): String = initializer["url"].asString
}