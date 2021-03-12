package playwright.browser

import com.google.gson.JsonObject
import playwright.processor.ChannelOwner
import playwright.processor.MessageProcessor
import playwright.websocket.WebSocketTransport

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

            return remoteBrowser.browser()
        }

    }
}