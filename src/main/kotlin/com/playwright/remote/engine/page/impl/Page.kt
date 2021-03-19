package com.playwright.remote.engine.page.impl

import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.core.enums.EventType.FILECHOOSER
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.browser.impl.BrowserContext
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.processor.ChannelOwner

class Page(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IPage {
    var ownedContext: IBrowserContext? = null
    private val browserContext = parent as BrowserContext
    private val listeners = object : ListenerCollection<EventType>() {
        override fun add(eventType: EventType, listener: UniversalConsumer) {
            if (eventType == FILECHOOSER) {
                updateFileChooserInterception(true)
            }
            super.add(eventType, listener)
        }

        override fun remove(eventType: EventType, listener: UniversalConsumer) {
            super.remove(eventType, listener)
            if (eventType == FILECHOOSER) {
                updateFileChooserInterception(false)
            }
        }
    }

    fun didClose() {
        browserContext.pages.remove(this)
        listeners.notify(CLOSE, this)
    }

    private fun updateFileChooserInterception(enabled: Boolean) {
        if (!listeners.hasListeners(FILECHOOSER)) {
            val params = JsonObject()
            params.addProperty("intercepted", enabled)
            sendMessage("setFileChooserInterceptedNoReply", params)
        }
    }
}