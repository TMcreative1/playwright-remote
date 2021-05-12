package com.playwright.remote.engine.browser.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.DISCONNECTED
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.options.NewPageOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.page.impl.Page
import com.playwright.remote.engine.parser.IParser.Companion.convert
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.processor.ChannelOwner
import okio.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class Browser(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowser {

    val contexts = hashSetOf<IBrowserContext>()
    private val listeners = ListenerCollection<EventType>()

    @Suppress("UNCHECKED_CAST")
    override fun onDisconnected(handler: (IBrowser) -> Unit) =
        listeners.add(DISCONNECTED, handler as UniversalConsumer)

    @Suppress("UNCHECKED_CAST")
    override fun offDisconnected(handler: (IBrowser) -> Unit) =
        listeners.remove(DISCONNECTED, handler as UniversalConsumer)

    override fun newContext(options: NewContextOptions?): IBrowserContext {
        val storageState: JsonObject? = getStorageState(options)
        val params = Gson().toJsonTree(options).asJsonObject

        if (storageState != null) {
            params.add("storageState", storageState)
        }

        addRecordHarPath(params, options)
        addRecordVideoDir(params, options)
        addViewPortSize(params, options)

        params.addProperty("sdkLanguage", "java")
        val result = sendMessage("newContext", params)
        val context = messageProcessor.getExistingObject<IBrowserContext>(
            result.asJsonObject["context"].asJsonObject["guid"].asString
        ) as BrowserContext
        if (options?.recordVideoDir != null) {
            context.videosDir = options.recordVideoDir
        }
        contexts.add(context)
        return context
    }

    override fun newPage(options: NewPageOptions?): IPage {
        val context =
            newContext(convert(options ?: NewContextOptions {}, NewContextOptions::class.java)) as BrowserContext
        val page = context.newPage() as Page
        page.ownedContext = context
        context.ownerPage = page
        return page
    }

    override fun close() {
        messageProcessor.close()
        notifyRemoteClosed()
    }

    override fun name(): String {
        return initializer["name"].asString
    }

    private fun getStorageState(options: NewContextOptions?): JsonObject? {
        if (options?.storageStatePath != null) {
            try {
                val bytes = Files.readAllBytes(options.storageStatePath!!)
                options.storageState = String(bytes, StandardCharsets.UTF_8)
                options.storageStatePath = null
            } catch (e: IOException) {
                throw PlaywrightException("Failed to read storage state from file", e)
            }
        }
        var storageState: JsonObject? = null
        if (options?.storageState != null) {
            storageState = fromJson(options.storageState, JsonObject::class.java)
            options.storageState = null
        }

        return storageState
    }

    private fun addRecordHarPath(params: JsonObject, options: NewContextOptions?) {
        if (options?.recordHarPath != null) {
            val recordHar = JsonObject()
            recordHar.addProperty("path", options.recordHarPath.toString())
            if (options.recordHarOmitContent != null) {
                recordHar.addProperty("omitContent", true)
            }
            params.remove("recordHarPath")
            params.remove("recordHarOmitContent")
            params.add("recordHar", recordHar)
        } else if (options?.recordHarOmitContent != null) {
            throw PlaywrightException("recordHarOmitContent is set but recordHarPath is null")
        }
    }

    private fun addRecordVideoDir(params: JsonObject, options: NewContextOptions?) {
        if (options?.recordVideoDir != null) {
            val recordVideo = JsonObject()
            recordVideo.addProperty("dir", options.recordVideoDir.toString())
            if (options.recordVideoSize != null) {
                recordVideo.add("size", Gson().toJsonTree(options.recordVideoSize))
            }
            params.remove("recordVideoDir")
            params.remove("recordVideoSize")
            params.add("recordVideo", recordVideo)
        } else if (options?.recordVideoSize != null) {
            throw PlaywrightException("recordVideoSize is set but recordVideoDir is null")
        }
    }

    private fun addViewPortSize(params: JsonObject, options: NewContextOptions?) {
        if (options?.viewportSize != null) {
            val size = params["viewportSize"]
            params.remove("viewportSize")
            params.add("viewport", size)
        } else {
            params.remove("viewportSize")
            params.addProperty("noDefaultViewport", true)
        }
    }

    fun notifyRemoteClosed() {
        contexts.forEach { context ->
            (context as BrowserContext).pages.forEach { page ->
                (page as Page).didClose()
            }
            context.didClose()
        }
        didClose()
    }

    override fun handleEvent(event: String, params: JsonObject) {
        if ("close" == event) {
            didClose()
        }
    }

    private fun didClose() =
        listeners.notify(DISCONNECTED, this)

}