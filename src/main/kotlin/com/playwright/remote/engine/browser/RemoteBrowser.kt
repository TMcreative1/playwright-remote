package com.playwright.remote.engine.browser

import com.google.gson.JsonObject
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.impl.Browser
import com.playwright.remote.engine.playwright.api.Playwright
import com.playwright.remote.engine.playwright.impl.IPlaywright
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.processor.MessageProcessor
import com.playwright.remote.engine.websocket.WebSocketTransport
import okio.IOException

class RemoteBrowser(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer) {

    private fun browser(): IBrowser =
        messageProcessor.getExistingObject(initializer["browser"].asJsonObject["guid"].asString)

    companion object {
        @JvmStatic
        fun connectWs(wsEndpoint: String): IBrowser {
            val webSocketTransport = WebSocketTransport(wsEndpoint)
            val messageProcessor = MessageProcessor(webSocketTransport)
            val playwright = messageProcessor.waitForObjectByGuid("Playwright")
            playwright as Playwright
            if (!playwright.initializer().has("preLaunchedBrowser")) {
                try {
                    messageProcessor.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                throw PlaywrightException("Malformed endpoint. Did you use launchServer method?")
            }
            playwright.initSharedSelectors(messageProcessor.getExistingObject("Playwright"))
            val browser =
                messageProcessor.getExistingObject<IBrowser>(playwright.initializer()["preLaunchedBrowser"].asJsonObject["guid"].asString)
            val connectionCloseListener: (WebSocketTransport) -> Unit = { (browser as Browser).notifyRemoteClosed() }
            webSocketTransport.onClose(connectionCloseListener)
            browser.onDisconnected {
                playwright.unregisterSelectors()
                webSocketTransport.offClose(connectionCloseListener)
                try {
                    messageProcessor.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return browser
        }

    }
}