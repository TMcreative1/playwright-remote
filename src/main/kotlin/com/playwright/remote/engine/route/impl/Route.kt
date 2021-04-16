package com.playwright.remote.engine.route.impl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.FulfillOptions
import com.playwright.remote.engine.options.ResumeOptions
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.api.IRoute
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.utils.Utils.Companion.mimeType
import okio.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.util.*

class Route(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IRoute {

    @JvmOverloads
    override fun abort(errorCode: String?) {
        val params = JsonObject()
        params.addProperty("errorCode", errorCode)
        sendMessage("abort", params)
    }

    @JvmOverloads
    override fun resume(options: ResumeOptions?) {
        val params = createParamsForResume(options ?: ResumeOptions {})
        sendMessage("continue", params)
    }

    @JvmOverloads
    override fun fulfill(options: FulfillOptions?) {
        val opts = options ?: FulfillOptions {}

        val status = opts.status ?: 200
        var body = ""
        var isBase64 = false
        var length = 0
        when {
            opts.path != null -> {
                try {
                    val buffer = Files.readAllBytes(opts.path!!)
                    body = Base64.getEncoder().encodeToString(buffer)
                    isBase64 = true
                    length = buffer.size
                } catch (e: IOException) {
                    throw PlaywrightException("Failed to read from file: ${opts.path}", e)
                }
            }
            opts.body != null -> {
                body = opts.body!!
                isBase64 = false
                length = body.encodeToByteArray().size
            }
            opts.bodyBytes != null -> {
                body = Base64.getEncoder().encodeToString(opts.bodyBytes)
                isBase64 = true
                length = opts.bodyBytes!!.size
            }
        }
        val headers = createHeaders(opts, length)
        val params = JsonObject()
        params.addProperty("status", status)
        params.add("headers", createJsonArrayFromMap(headers))
        params.addProperty("isBase64", isBase64)
        params.addProperty("body", body)
        sendMessage("fulfill", params)
    }

    override fun request(): IRequest =
        messageProcessor.getExistingObject(initializer["request"].asJsonObject["guid"].asString)

    private fun createJsonArrayFromMap(map: Map<String, String>): JsonArray {
        val array = JsonArray()
        map.entries.forEach {
            val item = JsonObject()
            item.addProperty("name", it.key)
            item.addProperty("value", it.value)
        }
        return array
    }

    private fun createParamsForResume(options: ResumeOptions): JsonObject {
        val params = JsonObject()
        if (options.url != null) {
            params.addProperty("url", options.url)
        }
        if (options.method != null) {
            params.addProperty("method", options.method)
        }
        if (options.headers != null) {
            params.add("headers", createJsonArrayFromMap(options.headers!!))
        }
        if (options.postData != null) {
            val bytes = when (options.postData) {
                is ByteArray -> options.postData as ByteArray
                is String -> (options.postData as String).toByteArray(UTF_8)
                else -> throw PlaywrightException("postData must be either String or ByteArray, found: ${options.postData!!.javaClass.name}")
            }
            params.addProperty("postData", Base64.getEncoder().encodeToString(bytes))
        }
        return params
    }

    private fun createHeaders(options: FulfillOptions, length: Int): LinkedHashMap<String, String> {
        val headers = linkedMapOf<String, String>()
        if (options.headers != null) {
            options.headers!!.forEach {
                headers[it.key] = it.value
            }
        }
        when {
            options.contentType != null -> headers["content-type"] = options.contentType!!
            options.path != null -> headers["content-type"] = mimeType(options.path!!)
        }

        if (length != 0 && !headers.containsKey("content-length")) {
            headers["content-length"] = length.toString()
        }
        return headers
    }
}