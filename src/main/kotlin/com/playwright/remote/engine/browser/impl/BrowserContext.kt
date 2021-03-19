package com.playwright.remote.engine.browser.impl

import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.core.enums.EventType.PAGE
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.processor.ChannelOwner
import java.nio.file.Path

class BrowserContext(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowserContext {
    private val browser = if (parent is IBrowser) parent as Browser else null
    var ownerPage: IPage? = null
    var videosDir: Path? = null
    val pages = arrayListOf<IPage>()
    private val listeners = ListenerCollection<EventType>()
    private var isClosedOrClosing: Boolean = false

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

    override fun close() {
        if (isClosedOrClosing) {
            return
        }
        isClosedOrClosing = true
        sendMessage("close")
    }

    fun didClose() {
        browser?.contexts?.remove(this)
        listeners.notify(CLOSE, this)
    }

}