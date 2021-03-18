package com.playwright.remote.playwright.browser

import com.google.gson.JsonObject
import com.playwright.remote.playwright.browser.impl.Browser
import com.playwright.remote.playwright.processor.ChannelOwner
import com.playwright.remote.playwright.processor.MessageProcessor
import com.playwright.remote.playwright.websocket.WebSocketTransport

class RemoteBrowser(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer) {

    private fun browser(): Browser =
        messageProcessor.getExistingObject(initializer.getAsJsonObject("browser")["guid"].asString)

    companion object {
        @JvmStatic
        fun connectWs(wsEndpoint: String): Browser {
            val webSocketTransport = WebSocketTransport(wsEndpoint)
            val messageProcessor = MessageProcessor(webSocketTransport)
            val remoteBrowser = messageProcessor.waitForObjectByGuid("remoteBrowser") as RemoteBrowser
            val browser = remoteBrowser.browser()

            val connectionCloseListener: (WebSocketTransport) -> Unit = { browser.notifyRemoteClosed() }
            webSocketTransport.onClose(connectionCloseListener)

            browser.onDisconnected {
                webSocketTransport.offClose(connectionCloseListener)
                webSocketTransport.closeConnection()
            }

            return browser
        }

    }
}