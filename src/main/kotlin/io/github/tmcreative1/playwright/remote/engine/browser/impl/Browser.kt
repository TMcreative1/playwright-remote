package io.github.tmcreative1.playwright.remote.engine.browser.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.core.enums.DeviceDescriptors
import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.core.enums.EventType.DISCONNECTED
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowser
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowserContext
import io.github.tmcreative1.playwright.remote.engine.browser.selector.api.ISharedSelectors
import io.github.tmcreative1.playwright.remote.engine.browser.selector.impl.SharedSelectors
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection
import io.github.tmcreative1.playwright.remote.engine.listener.UniversalConsumer
import io.github.tmcreative1.playwright.remote.engine.options.NewContextOptions
import io.github.tmcreative1.playwright.remote.engine.options.NewPageOptions
import io.github.tmcreative1.playwright.remote.engine.options.StartTracingOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.page.impl.Page
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.convert
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.fromJson
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import okio.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*

class Browser(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowser {
    private val sharedSelectors = SharedSelectors()

    val contexts = hashSetOf<IBrowserContext>()
    private val listeners = ListenerCollection<EventType>()

    @Suppress("UNCHECKED_CAST")
    override fun onDisconnected(handler: (IBrowser) -> Unit) =
        listeners.add(DISCONNECTED, handler as UniversalConsumer)

    @Suppress("UNCHECKED_CAST")
    override fun offDisconnected(handler: (IBrowser) -> Unit) =
        listeners.remove(DISCONNECTED, handler as UniversalConsumer)


    override fun newContext(options: NewContextOptions?): IBrowserContext {
        val opt = options ?: NewContextOptions {}
        val storageState: JsonObject? = getStorageState(opt)
        val params = gson().toJsonTree(opt).asJsonObject

        if (storageState != null) {
            params.add("storageState", storageState)
        }

        addRecordHarPath(params, opt)
        addRecordVideoDir(params, opt)
        addViewPortSize(params, opt)

        val result = sendMessage("newContext", params)
        val context = messageProcessor.getExistingObject<IBrowserContext>(
            result!!.asJsonObject["context"].asJsonObject["guid"].asString
        ) as BrowserContext
        if (opt.recordVideoDir != null) {
            context.videosDir = opt.recordVideoDir
        }
        if (opt.baseURL != null) {
            context.setBaseUrl(opt.baseURL!!)
        }
        context.recordHarPath = opt.recordHarPath
        contexts.add(context)
        return context
    }

    override fun newPage(options: NewPageOptions?, device: DeviceDescriptors?): IPage {
        val contextOptions = convert(options ?: NewContextOptions {}, NewContextOptions::class.java)

        if (device != null) {
            contextOptions.userAgent = device.userAgent
            contextOptions.viewportSize = device.viewport
            contextOptions.deviceScaleFactor = device.deviceScaleFactor
            contextOptions.screenSize = device.screen
            contextOptions.isMobile = device.isMobile
            contextOptions.hasTouch = device.hasTouch
        }

        val context =
            newContext(contextOptions) as BrowserContext
        val page = context.newPage() as Page
        page.ownedContext = context
        context.ownerPage = page
        return page
    }

    override fun close() {
        messageProcessor.close()
    }

    override fun name(): String {
        return initializer["name"].asString
    }

    override fun contexts(): List<IBrowserContext> {
        return contexts.toList()
    }

    override fun version(): String {
        return initializer["version"].asString
    }

    override fun selectors(): ISharedSelectors {
        return sharedSelectors
    }

    override fun startTracing(page: IPage?, options: StartTracingOptions?) {
        val params = gson().toJsonTree(options ?: StartTracingOptions {}).asJsonObject
        if (page != null) {
            val jsonPage = JsonObject()
            jsonPage.addProperty("guid", (page as Page).guid)
            params.add("page", jsonPage)
        }
        sendMessage("startTracing", params)
    }

    override fun stopTracing(): ByteArray {
        val json = sendMessage("stopTracing")!!.asJsonObject
        return Base64.getDecoder().decode(json["binary"].asString)
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
                recordVideo.add("size", gson().toJsonTree(options.recordVideoSize))
            }
            params.remove("recordVideoDir")
            params.remove("recordVideoSize")
            params.add("recordVideo", recordVideo)
        } else if (options?.recordVideoSize != null) {
            throw PlaywrightException("recordVideoSize is set but recordVideoDir is null")
        }
    }

    private fun addViewPortSize(params: JsonObject, options: NewContextOptions?) {
        if (options!!.isViewPortNotNull()) {
            if (options.isPresentViewPort()) {
                val size = params["viewportSize"]
                params.remove("viewportSize")
                params.add("viewport", size)
            } else {
                params.remove("viewportSize")
                params.addProperty("noDefaultViewport", true)
            }
        }
    }

    fun notifyRemoteClosed() {
        contexts.toTypedArray().forEach { context ->
            (context as BrowserContext).pages.toTypedArray().forEach { page ->
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