package com.playwright.remote.engine.browser

import com.google.gson.JsonObject
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.impl.Browser
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.processor.MessageProcessor
import com.playwright.remote.engine.websocket.WebSocketTransport

class RemoteBrowser(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer) {

    private fun browser(): IBrowser =
        messageProcessor.getExistingObject(initializer["browser"].asJsonObject["guid"].asString)

    companion object {
        @JvmStatic
        fun connectWs(wsEndpoint: String): IBrowser {
            val webSocketTransport = WebSocketTransport(wsEndpoint)
            val messageProcessor = MessageProcessor(webSocketTransport)
            val remoteBrowser = messageProcessor.waitForObjectByGuid("remoteBrowser") as RemoteBrowser
            val browser = remoteBrowser.browser() as Browser

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