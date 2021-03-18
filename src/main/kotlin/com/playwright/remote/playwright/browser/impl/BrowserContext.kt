package com.playwright.remote.playwright.browser.impl

import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.playwright.browser.api.IBrowser
import com.playwright.remote.playwright.browser.api.IBrowserContext
import com.playwright.remote.playwright.listener.ListenerCollection
import com.playwright.remote.playwright.page.api.IPage
import com.playwright.remote.playwright.processor.ChannelOwner
import java.nio.file.Path

class BrowserContext(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBrowserContext {
    val browser = if (parent is IBrowser) parent as Browser else null
    var ownerPage: IPage? = null
    var videosDir: Path? = null
    val pages = arrayListOf<IPage>()
    private val listeners = ListenerCollection<EventType>()

    override fun newPage(): IPage {
        if (ownerPage != null) {
            throw PlaywrightException("Please use browser.newContext()")
        }
        val jsonObject = sendMessage("newPage").asJsonObject
        return messageProcessor.getExistingObject(jsonObject.getAsJsonObject("page").get("guid").asString)
    }


    override fun close() {
        TODO("Not yet implemented")
    }

    fun didClose() {
        browser?.contexts?.remove(this)
        listeners.notify(CLOSE, this)
    }

}