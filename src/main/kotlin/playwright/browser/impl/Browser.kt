package playwright.browser.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import core.enums.EventType
import core.enums.EventType.DISCONNECTED
import core.exceptions.PlaywrightException
import okio.IOException
import playwright.browser.api.IBrowser
import playwright.browser.api.IBrowserContext
import playwright.listener.ListenerCollection
import playwright.options.NewContextOptions
import playwright.options.NewPageOptions
import playwright.page.api.IPage
import playwright.parser.IParser.Companion.convert
import playwright.parser.IParser.Companion.fromJson
import playwright.processor.ChannelOwner
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class Browser(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowser {

    private val contexts = hashSetOf<IBrowserContext>()
    private val listeners = ListenerCollection<EventType, IBrowser>()


    override fun onDisconnected(handler: (IBrowser) -> Unit) = listeners.add(DISCONNECTED, handler)

    override fun newContext(options: NewContextOptions?): BrowserContext {
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
        val context = messageProcessor.getExistingObject<BrowserContext>(
            result.asJsonObject.getAsJsonObject("context").get("guid").asString
        )
        if (options?.recordVideoDir != null) {
            context.videosDir = options.recordVideoDir
        }
        contexts.add(context)
        return context
    }

    override fun newPage(options: NewPageOptions?): IPage {
        val context = newContext(convert(options, NewContextOptions::class.java) ?: NewContextOptions {})
        val page = context.newPage()
        page.ownedContext = context
        context.ownerPage = page
        return page
    }

    override fun close() {
        messageProcessor.close()
    }

    private fun getStorageState(options: NewContextOptions?): JsonObject? {
        if (options?.storageStatePath != null) {
            try {
                val bytes = Files.readAllBytes(options.storageStatePath)
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
            if (options?.recordHarOmitContent != null) {
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
            val size = params.get("viewportSize")
            params.remove("viewportSize")
            params.add("viewport", size)
        } else {
            params.remove("viewportSize")
            params.addProperty("noDefaultViewport", true)
        }
    }
}