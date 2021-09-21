package io.github.tmcreative1.playwright.remote.engine.route.request.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.domain.request.HttpHeader
import io.github.tmcreative1.playwright.remote.domain.request.Sizes
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.route.RawHeader
import io.github.tmcreative1.playwright.remote.engine.route.request.Timing
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest
import io.github.tmcreative1.playwright.remote.engine.route.response.api.IResponse
import io.github.tmcreative1.playwright.remote.engine.route.response.impl.Response
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
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
    private var rawHeader: RawHeader? = null
    val headers: RawHeader
    var failure: String = ""
    var timing: Timing = Timing {}
    var didFailOrFinish: Boolean? = null

    init {
        if (initializer.has("redirectedFrom")) {
            redirectedFrom =
                messageProcessor.getExistingObject(initializer["redirectedFrom"].asJsonObject["guid"].asString)
            (redirectedFrom as Request).redirectedTo = this
        }
        headers = RawHeader(gson().fromJson(initializer["headers"].asJsonArray, Array<HttpHeader>::class.java).toList())
        if (initializer.has("postData")) {
            postData = Base64.getDecoder().decode(initializer["postData"].asString)
        }
    }

    override fun failure(): String = failure

    override fun frame(): IFrame =
        messageProcessor.getExistingObject(initializer["frame"].asJsonObject["guid"].asString)

    override fun headers(): Map<String, String?> = headers.headers()

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

    override fun allHeaders(): Map<String, String?> = rawHeader().headers()

    override fun headersArray(): List<HttpHeader> = rawHeader().headersArray()

    override fun headerValue(name: String): String? = rawHeader().get(name)

    override fun sizes(): Sizes {
        val response = response() ?: throw PlaywrightException("Unable to fetch sizes for failed request")
        val json = (response as Response).sendMessage("sizes")!!.asJsonObject
        return gson().fromJson(json["sizes"].asJsonObject, Sizes::class.java)
    }

    private fun rawHeader(): RawHeader {
        if (rawHeader != null) return rawHeader!!

        val response = response()
        if (response == null) headers
        val result = (response as Response).sendMessage("rawRequestHeaders")!!.asJsonObject
        val rawHeaderJson = result["headers"].asJsonArray

        rawHeader = RawHeader(gson().fromJson(rawHeaderJson, Array<HttpHeader>::class.java).toList())
        return rawHeader!!
    }
}