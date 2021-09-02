package io.github.tmcreative1.playwright.remote.engine.browser.impl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.core.enums.EventType.*
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowser
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowserContext
import io.github.tmcreative1.playwright.remote.engine.callback.api.IBindingCall
import io.github.tmcreative1.playwright.remote.engine.callback.api.IBindingCallback
import io.github.tmcreative1.playwright.remote.engine.callback.api.IBindingCallback.ISource
import io.github.tmcreative1.playwright.remote.engine.callback.api.IFunctionCallback
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection
import io.github.tmcreative1.playwright.remote.engine.listener.UniversalConsumer
import io.github.tmcreative1.playwright.remote.engine.options.*
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForPageOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.page.impl.Page
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.fromJson
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.route.Router
import io.github.tmcreative1.playwright.remote.engine.route.UrlMatcher
import io.github.tmcreative1.playwright.remote.engine.route.api.IRoute
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import io.github.tmcreative1.playwright.remote.engine.waits.TimeoutSettings
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitContextClose
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitEvent
import io.github.tmcreative1.playwright.remote.engine.waits.impl.WaitRace
import io.github.tmcreative1.playwright.remote.utils.Utils.Companion.writeToFile
import okio.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.readAllBytes
import java.nio.file.Path
import java.util.regex.Pattern

class BrowserContext(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowserContext {
    private val browser = if (parent is IBrowser) parent as Browser else null
    var ownerPage: IPage? = null
    var videosDir: Path? = null
    val pages = arrayListOf<IPage>()
    private val listeners = ListenerCollection<EventType>()
    private var isClosedOrClosing: Boolean = false
    val timeoutSettings = TimeoutSettings()
    val routes = Router()
    val bindings = hashMapOf<String, IBindingCallback>()

    @Suppress("UNCHECKED_CAST")
    override fun onClose(handler: (IBrowserContext) -> Unit) {
        listeners.add(CLOSE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offClose(handler: (IBrowserContext) -> Unit) {
        listeners.remove(CLOSE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onPage(handler: (IPage) -> Unit) {
        listeners.add(PAGE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offPage(handler: (IPage) -> Unit) {
        listeners.remove(PAGE, handler as UniversalConsumer)
    }

    override fun newPage(): IPage {
        if (ownerPage != null) {
            throw PlaywrightException("Please use browser.newContext()")
        }
        val jsonObject = sendMessage("newPage")!!.asJsonObject
        return messageProcessor.getExistingObject(jsonObject.getAsJsonObject("page").get("guid").asString)
    }

    override fun pages(): List<IPage> {
        return pages
    }

    private fun <T> waitForEventWithTimeout(eventType: EventType, timeout: Double?, code: () -> Unit): T? {
        val waitList = arrayListOf<IWait<T>>()
        waitList.add(WaitEvent(listeners, eventType))
        waitList.add(WaitContextClose(listeners))
        waitList.add(timeoutSettings.createWait(timeout))
        return runUtil(WaitRace(waitList), code)
    }

    override fun waitForPage(options: WaitForPageOptions?, callback: () -> Unit): IPage? =
        waitForEventWithTimeout(PAGE, (options ?: WaitForPageOptions {}).timeout, callback)

    override fun close() {
        if (isClosedOrClosing) {
            return
        }
        isClosedOrClosing = true
        sendMessage("close")
    }

    override fun addCookies(cookies: List<Cookie>) {
        val params = JsonObject()
        params.add("cookies", gson().toJsonTree(cookies))
        sendMessage("addCookies", params)
    }

    override fun addInitScript(script: String) {
        val params = JsonObject()
        params.addProperty("source", script)
        sendMessage("addInitScript", params)
    }

    override fun addInitScript(path: Path) = try {
        val bytes = readAllBytes(path)
        addInitScript(String(bytes, UTF_8))
    } catch (e: IOException) {
        throw PlaywrightException("Failed to read script from file", e)
    }

    override fun browser(): IBrowser {
        return browser!!
    }

    override fun clearCookies() {
        sendMessage("clearCookies")
    }

    override fun clearPermissions() {
        sendMessage("clearPermissions")
    }


    override fun cookies(url: String?): List<Cookie> =
        cookies(if (url != null) listOf(url) else emptyList())

    override fun cookies(urls: List<String>): List<Cookie> {
        val params = JsonObject()
        params.add("urls", gson().toJsonTree(urls))
        val json = sendMessage("cookies", params)!!.asJsonObject
        val cookies = fromJson(json["cookies"].asJsonArray, Array<Cookie>::class.java)
        return cookies.toList()
    }

    override fun exposeBinding(name: String, callback: IBindingCallback, options: ExposeBindingOptions?) {
        if (bindings.containsKey(name)) {
            throw PlaywrightException("Function $name has been already registered")
        }
        for (page in pages) {
            if ((page as Page).bindings.containsKey(name)) {
                throw PlaywrightException("Function $name has been already registered in one of the pages")
            }
        }
        bindings[name] = callback

        val params = JsonObject()
        params.addProperty("name", name)
        if (options?.handle != null && options.handle!!) {
            params.addProperty("needsHandle", true)
        }
        sendMessage("exposeBinding", params)
    }

    override fun exposeFunction(name: String, callback: IFunctionCallback) {
        exposeBinding(name) { _: ISource, args: Array<Any> -> callback.call(args) }
    }

    override fun grantPermissions(permissions: List<String>?, options: GrantPermissionsOptions) {
        val params = gson().toJsonTree(options).asJsonObject
        params.add("permissions", gson().toJsonTree(permissions ?: emptyList<String>()))
        sendMessage("grantPermissions", params)
    }

    override fun route(url: String, handler: (IRoute) -> Unit) =
        route(UrlMatcher(url), handler)

    override fun route(url: Pattern, handler: (IRoute) -> Unit) =
        route(UrlMatcher(url), handler)

    override fun route(url: (String) -> Boolean, handler: (IRoute) -> Unit) =
        route(UrlMatcher(url), handler)

    private fun route(matcher: UrlMatcher, handler: (IRoute) -> Unit) {
        routes.add(matcher, handler)
        if (routes.size() == 1) {
            val params = JsonObject()
            params.addProperty("enabled", true)
            sendMessage("setNetworkInterceptionEnabled", params)
        }
    }

    override fun setDefaultNavigationTimeout(timeout: Double) {
        timeoutSettings.defaultNavigationTimeout = timeout
        val params = JsonObject()
        params.addProperty("timeout", timeout)
        sendMessage("setDefaultNavigationTimeoutNoReply", params)
    }

    override fun setDefaultTimeout(timeout: Double) {
        timeoutSettings.defaultTimeout = timeout
        val params = JsonObject()
        params.addProperty("timeout", timeout)
        sendMessage("setDefaultTimeoutNoReply", params)
    }

    override fun setExtraHttpHeaders(headers: Map<String, String>) {
        val params = JsonObject()
        val jsonHeaders = JsonArray()
        for (entry in headers.entries) {
            val header = JsonObject()
            header.addProperty("name", entry.key)
            header.addProperty("value", entry.value)
            jsonHeaders.add(header)
        }
        params.add("headers", jsonHeaders)
        sendMessage("setExtraHTTPHeaders", params)
    }

    override fun setGeolocation(geolocation: Geolocation?) {
        val params = JsonObject()
        if (geolocation != null) {
            params.add("geolocation", gson().toJsonTree(geolocation))
        }
        sendMessage("setGeolocation", params)
    }

    override fun setOffline(isOffline: Boolean) {
        val params = JsonObject()
        params.addProperty("offline", isOffline)
        sendMessage("setOffline", params)
    }

    override fun storageState(options: StorageStateOptions?): String {
        val json = sendMessage("storageState")
        val storageState = json.toString()
        if (options?.path != null) {
            writeToFile(storageState.toByteArray(UTF_8), options.path!!)
        }
        return storageState
    }

    override fun unRoute(url: String, handler: ((IRoute) -> Unit)?) {
        unRoute(UrlMatcher(url), handler)
    }

    override fun unRoute(url: Pattern, handler: ((IRoute) -> Unit)?) {
        unRoute(UrlMatcher(url), handler)
    }

    override fun unRoute(url: (String) -> Boolean, handler: ((IRoute) -> Unit)?) {
        unRoute(UrlMatcher(url), handler)
    }

    override fun pause() {
        sendMessage("pause")
    }

    private fun unRoute(matcher: UrlMatcher, handler: ((IRoute) -> Unit)?) {
        routes.remove(matcher, handler)
        if (routes.size() == 0) {
            val params = JsonObject()
            params.addProperty("enabled", false)
            sendMessage("setNetworkInterceptionEnabled", params)
        }
    }

    fun didClose() {
        isClosedOrClosing = true
        browser?.contexts?.remove(this)
        listeners.notify(CLOSE, this)
    }

    override fun handleEvent(event: String, params: JsonObject) {
        when (event) {
            "route" -> {
                val route = messageProcessor.getExistingObject<IRoute>(params["route"].asJsonObject["guid"].asString)
                if (!routes.handle(route)) {
                    route.resume()
                }
            }
            "page" -> {
                val page = messageProcessor.getExistingObject<IPage>(params["page"].asJsonObject["guid"].asString)
                pages.add(page)
                listeners.notify(PAGE, page)
            }
            "bindingCall" -> {
                val bindingCall =
                    messageProcessor.getExistingObject<IBindingCall>(params["binding"].asJsonObject["guid"].asString)
                val binding = bindings[bindingCall.name()]
                if (binding != null) {
                    bindingCall.call(binding)
                }
            }
            else -> {
                if (event == "close") {
                    didClose()
                }
            }
        }
    }
}