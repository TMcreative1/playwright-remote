package com.playwright.remote.engine.route.request.impl

import com.google.gson.JsonObject
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.request.Timing
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.response.api.IResponse
import java.nio.charset.StandardCharsets
import java.util.*

class Request(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IRequest {
    private var postData: ByteArray? = null
    private var redirectedFrom: IRequest? = null
    private var redirectedTo: IRequest? = null
    val headers = hashMapOf<String, String>()
    var failure: String = ""
    var timing: Timing = Timing {}

    init {
        if (initializer.has("redirectedFrom")) {
            redirectedFrom =
                messageProcessor.getExistingObject(initializer["redirectedFrom"].asJsonObject["guid"].asString)
            (redirectedFrom as Request).redirectedTo = this
        }
        for (element in initializer["headers"].asJsonArray) {
            val item = element.asJsonObject
            headers[item["name"].asString.lowercase()] = item["value"].asString
        }
        if (initializer.has("postData")) {
            postData = Base64.getDecoder().decode(initializer["postData"].asString)
        }
    }

    override fun failure(): String = failure

    override fun frame(): IFrame =
        messageProcessor.getExistingObject(initializer["frame"].asJsonObject["guid"].asString)

    override fun headers(): Map<String, String> = headers

    override fun isNavigationRequest(): Boolean = initializer["isNavigationRequest"].asBoolean

    override fun method(): String = initializer["method"].asString

    override fun postData(): String? {
        if (postData == null) {
            return null
        }
        return String(postData!!, StandardCharsets.UTF_8)
    }

    override fun postDataBuffer(): ByteArray? = postData

    override fun redirectedFrom(): IRequest? = redirectedFrom

    override fun redirectedTo(): IRequest? = redirectedTo

    override fun resourceType(): String = initializer["resourceType"].asString

    override fun response(): IResponse? {
        val result = sendMessage("response")!!.asJsonObject
        if (!result.has("response")) {
            return null
        }

        return messageProcessor.getExistingObject(result["response"].asJsonObject["guid"].asString)
    }

    override fun timing(): Timing = timing

    override fun url(): String = initializer["url"].asString

    fun finalRequest(): IRequest = if (redirectedTo != null) (redirectedTo as Request).finalRequest() else this
}