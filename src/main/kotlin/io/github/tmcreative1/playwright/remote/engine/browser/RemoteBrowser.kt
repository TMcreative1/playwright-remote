package io.github.tmcreative1.playwright.remote.engine.browser

import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowser
import io.github.tmcreative1.playwright.remote.engine.browser.impl.Browser
import io.github.tmcreative1.playwright.remote.engine.browser.selector.api.ISelectors
import io.github.tmcreative1.playwright.remote.engine.processor.MessageProcessor
import io.github.tmcreative1.playwright.remote.engine.websocket.WebSocketTransport
import okio.IOException

class RemoteBrowser {

    companion object {
        @JvmStatic
        fun connectWs(wsEndpoint: String): IBrowser {
            val webSocketTransport = WebSocketTransport(wsEndpoint)
            val messageProcessor = MessageProcessor(webSocketTransport)
            val browser = messageProcessor.waitForLaunchedBrowser() as IBrowser
            val selectors = messageProcessor.waitForSelectors() as ISelectors
            browser.selectors().addChannel(selectors)
            val connectionCloseListener: (WebSocketTransport) -> Unit = { (browser as Browser).notifyRemoteClosed() }
            webSocketTransport.onClose(connectionCloseListener)
            browser.onDisconnected {
                browser.selectors().removeChannel(selectors)
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