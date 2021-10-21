package io.github.tmcreative1.playwright.remote.engine.browser

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowser
import io.github.tmcreative1.playwright.remote.engine.browser.impl.Browser
import io.github.tmcreative1.playwright.remote.engine.browser.selector.api.ISelectors
import io.github.tmcreative1.playwright.remote.engine.processor.MessageProcessor
import io.github.tmcreative1.playwright.remote.engine.transport.impl.JsonPipe
import io.github.tmcreative1.playwright.remote.engine.transport.impl.WebSocketTransport
import okio.IOException

class RemoteBrowser {

    companion object {
        @JvmStatic
        fun connectWs(wsEndpoint: String): IBrowser {
            val webSocketTransport = WebSocketTransport(wsEndpoint)
            val messageProcessor = MessageProcessor(webSocketTransport)
            var browser = messageProcessor.waitForLaunchedBrowser() as IBrowser
            val params = JsonObject()
            params.addProperty("wsEndpoint", wsEndpoint)
            val json = messageProcessor.sendMessage(
                messageProcessor.getBrowserTypeGuid(browser.name()),
                "connect",
                params
            )!!.asJsonObject
            val pipe = messageProcessor.getExistingObject<JsonPipe>(json["pipe"].asJsonObject["guid"].asString)
            val pipeProcessor = MessageProcessor(pipe)
            browser = pipeProcessor.waitForLaunchedBrowser() as IBrowser
            val selectors = pipeProcessor.waitForSelectors() as ISelectors
            browser.selectors().addChannel(selectors)
            val connectionCloseListener: (JsonPipe) -> Unit = { (browser as Browser).notifyRemoteClosed() }
            pipe.onClose(connectionCloseListener)
            browser.onDisconnected {
                browser.selectors().removeChannel(selectors)
                pipe.offClose(connectionCloseListener)
                try {
                    pipe.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return browser
        }

    }
}