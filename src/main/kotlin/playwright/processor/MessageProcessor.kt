package playwright.processor

import com.google.gson.JsonObject
import core.enums.MethodType.CREATE
import core.enums.ObjectType.*
import core.exceptions.PlaywrightException
import playwright.browser.Browser
import playwright.browser.RemoteBrowser
import playwright.browser.Selectors
import playwright.parser.MessageParser.Companion.parseMessage
import playwright.transport.ITransport
import playwright.websocket.Message

class MessageProcessor(private val transport: ITransport) {
    private class Root(messageProcessor: MessageProcessor) : ChannelOwner(messageProcessor, "", "")

    private val objects = hashMapOf<String, ChannelOwner>()
    private val root = Root(this)

    fun registerObject(guid: String, obj: ChannelOwner) {
        objects[guid] = obj
    }

    fun waitForObjectByGuid(guid: String): ChannelOwner? {
        while (!objects.containsKey(guid)) {
            processMessage()
        }
        return objects[guid]
    }

    fun <T> getExistingObject(guid: String): T =
        objects[guid] as T ?: throw PlaywrightException("Object doesn't exist: $guid")

    private fun processMessage() {
        val messageStr = transport.pollMessage() ?: return
        val message = parseMessage(messageStr)
        dispatch(message)
    }

    private fun dispatch(message: Message) {
        if (message.id != 0) {

        }

        if (message.method == null) {
            return
        }

        when (message.method) {
            CREATE.type -> {
                createRemoteObject(message.guid, message.params)
            }
        }
    }

    private fun createRemoteObject(parentGuid: String, params: JsonObject) {
        val type = params["type"].asString
        val guid = params["guid"].asString

        val parent = objects[parentGuid]
            ?: throw PlaywrightException("Cannot find parent object $parentGuid to create $guid")
        val initializer = params["initializer"].asJsonObject
        when (type) {
            BROWSER.type -> Browser(parent, type, guid, initializer)
            SELECTORS.type -> Selectors(parent, type, guid, initializer)
            REMOTE_BROWSER.type -> RemoteBrowser(parent, type, guid, initializer)
            else -> throw PlaywrightException("Unknown type $type")
        }
    }
}