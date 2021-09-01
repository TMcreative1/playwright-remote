package com.playwright.remote.engine.browser

import com.google.gson.JsonObject
import com.playwright.remote.engine.browser.api.IBrowser
import com.playwright.remote.engine.browser.impl.Browser
import com.playwright.remote.engine.browser.impl.BrowserContext
import com.playwright.remote.engine.browser.selector.api.ISelectors
import com.playwright.remote.engine.download.stream.api.IStream
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.processor.MessageProcessor
import com.playwright.remote.engine.websocket.WebSocketTransport
import com.playwright.remote.utils.Utils.Companion.writeToFile
import java.nio.file.Paths

class RemoteBrowser(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer) {

    private fun browser(): IBrowser =
        messageProcessor.getExistingObject(initializer["browser"].asJsonObject["guid"].asString)

    private fun selectors(): ISelectors =
        messageProcessor.getExistingObject(initializer["selectors"].asJsonObject["guid"].asString)

    companion object {
        @JvmStatic
        fun connectWs(wsEndpoint: String): IBrowser {
            val webSocketTransport = WebSocketTransport(wsEndpoint)
            val messageProcessor = MessageProcessor(webSocketTransport)
            val remoteBrowser = messageProcessor.waitForObjectByGuid("remoteBrowser") as RemoteBrowser
            val browser = remoteBrowser.browser() as Browser
            val selectors = remoteBrowser.selectors()
            browser.selectors().addChannel(selectors)

            val connectionCloseListener: (WebSocketTransport) -> Unit = { browser.notifyRemoteClosed() }
            webSocketTransport.onClose(connectionCloseListener)

            browser.onDisconnected {
                browser.selectors().removeChannel(selectors)
                webSocketTransport.offClose(connectionCloseListener)
                webSocketTransport.closeConnection()
            }

            return browser
        }
    }

    override fun handleEvent(event: String, params: JsonObject) {
        when (event) {
            "video" -> {
                val stream = messageProcessor.getExistingObject<IStream>(params["stream"].asJsonObject["guid"].asString)
                val inputStream = stream.stream()
                val context =
                    messageProcessor.getExistingObject<BrowserContext>(params["context"].asJsonObject["guid"].asString)
                writeToFile(inputStream, Paths.get("${context.videosDir}/${params["relativePath"].asString}"))
            }
        }
    }
}