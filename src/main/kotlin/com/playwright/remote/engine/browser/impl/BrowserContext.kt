package com.playwright.remote.engine.browser.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.core.enums.EventType.PAGE
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.options.Cookie
import com.playwright.remote.engine.options.WaitForPageOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.route.UrlMatcher
import com.playwright.remote.engine.route.api.IRoute
import com.playwright.remote.engine.waits.TimeoutSettings
import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.WaitContextClose
import com.playwright.remote.engine.waits.impl.WaitEvent
import com.playwright.remote.engine.waits.impl.WaitRace
import okio.IOException
import java.nio.file.Files.readAllBytes
import java.nio.file.Path
import java.util.regex.Pattern
import kotlin.text.Charsets.UTF_8

class BrowserContext(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowserContext {
    private val browser = if (parent is IBrowser) parent as Browser else null
    var ownerPage: IPage? = null
    var videosDir: Path? = null
    val pages = arrayListOf<IPage>()
    private val listeners = ListenerCollection<EventType>()
    private var isClosedOrClosing: Boolean = false
    private val timeoutSettings = TimeoutSettings()
    private val routes = Router()

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
        val jsonObject = sendMessage("newPage").asJsonObject
        return messageProcessor.getExistingObject(jsonObject.getAsJsonObject("page").get("guid").asString)
    }

    private fun <T> waitForEventWithTimeout(eventType: EventType, timeout: Double?, code: () -> Unit): T {
        val waitList = arrayListOf<IWait<T>>()
        waitList.add(WaitEvent(listeners, eventType))
        waitList.add(WaitContextClose(listeners))
        waitList.add(timeoutSettings.createWait(timeout))
        return runUtil(WaitRace(waitList), code)
    }

    override fun waitForPage(options: WaitForPageOptions?, callback: () -> Unit): IPage =
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
        params.add("cookies", Gson().toJsonTree(cookies))
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

    fun didClose() {
        browser?.contexts?.remove(this)
        listeners.notify(CLOSE, this)
    }

}