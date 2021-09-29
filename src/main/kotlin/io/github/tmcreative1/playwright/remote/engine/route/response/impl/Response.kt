package io.github.tmcreative1.playwright.remote.engine.route.response.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.domain.request.HttpHeader
import io.github.tmcreative1.playwright.remote.domain.response.SecurityDetails
import io.github.tmcreative1.playwright.remote.domain.response.ServerAddress
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.page.impl.Page
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.route.RawHeader
import io.github.tmcreative1.playwright.remote.engine.route.request.Timing
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest
import io.github.tmcreative1.playwright.remote.engine.route.request.impl.Request
import io.github.tmcreative1.playwright.remote.engine.route.response.api.IResponse
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitNever
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitRace
import java.nio.charset.StandardCharsets
import java.util.*

class Response(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IResponse {
    private val headers: RawHeader =
        RawHeader(gson().fromJson(initializer["headers"].asJsonArray, Array<HttpHeader>::class.java).toList())
    private val request: IRequest
    private var rawHeader: RawHeader? = null

    init {
        request = messageProcessor.getExistingObject(initializer["request"].asJsonObject["guid"].asString) as Request
        request.timing = gson().fromJson(initializer["timing"], Timing::class.java)
    }

    override fun body(): ByteArray {
        val json = sendMessage("body")!!.asJsonObject
        return Base64.getDecoder().decode(json["binary"].asString)
    }

    override fun finished(): String {
        val waits = arrayListOf<IWait<String>>()
        waits.add(object : WaitNever<String>() {
            override fun isFinished(): Boolean {
                val result = (request as Request).didFailOrFinish
                return result ?: false
            }

            override fun get(): String {
                return request.failure()
            }
        })
        val page = request.frame().page() as Page
        waits.add(page.createWaitForCloseHelper())
        waits.add(page.createWaitTimeout(null))
        runUtil(WaitRace(waits)) {}
        return request.failure()
    }

    override fun frame(): IFrame = request().frame()

    override fun headers(): Map<String, String?> = headers.headers()

    override fun ok(): Boolean = status() == 0 || (status() in 200..299)

    override fun request(): IRequest = request

    override fun securityDetails(): SecurityDetails? {
        val json = sendMessage("securityDetails")!!.asJsonObject
        if (json.has("value")) {
            return gson().fromJson(json["value"], SecurityDetails::class.java)
        }
        return null
    }

    override fun serverAddress(): ServerAddress? {
        val json = sendMessage("serverAddr")!!.asJsonObject
        if (json.has("value")) {
            return gson().fromJson(json["value"], ServerAddress::class.java)
        }
        return null
    }

    override fun status(): Int = initializer["status"].asInt

    override fun statusText(): String = initializer["statusText"].asString

    override fun text(): String = String(body(), StandardCharsets.UTF_8)

    override fun url(): String = initializer["url"].asString

    override fun allHeaders(): Map<String, String?> = rawHeader().headers()

    override fun headersArray(): List<HttpHeader> = rawHeader().headersArray()

    override fun headerValue(name: String): String? = rawHeader().get(name)

    override fun headerValues(name: String): List<String>? = rawHeader().getAll(name)

    private fun rawHeader(): RawHeader {
        if (rawHeader == null) {
            val json = sendMessage("rawResponseHeaders")!!.asJsonObject
            rawHeader = RawHeader(gson().fromJson(json["headers"].asJsonArray, Array<HttpHeader>::class.java).toList())
        }
        return rawHeader!!
    }
}