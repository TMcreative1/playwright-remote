package playwright.page.impl

import com.google.gson.JsonObject
import core.enums.EventType
import core.enums.EventType.CLOSE
import core.enums.EventType.FILECHOOSER
import playwright.browser.api.IBrowserContext
import playwright.browser.impl.BrowserContext
import playwright.listener.ListenerCollection
import playwright.listener.UniversalConsumer
import playwright.page.api.IPage
import playwright.processor.ChannelOwner

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