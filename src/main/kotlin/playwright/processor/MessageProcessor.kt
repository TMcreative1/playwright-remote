package playwright.processor

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import core.enums.MethodType.CREATE
import core.enums.MethodType.DISPOSE
import core.enums.ObjectType.*
import core.exceptions.DriverException
import core.exceptions.PlaywrightException
import core.exceptions.TimeoutException
import domain.message.Message
import playwright.browser.RemoteBrowser
import playwright.browser.impl.Browser
import playwright.browser.impl.BrowserContext
import playwright.browser.impl.Selectors
import playwright.parser.IParser.Companion.fromJson
import playwright.parser.IParser.Companion.toJson
import playwright.transport.ITransport
import playwright.waits.impl.WaitResult

class MessageProcessor(private val transport: ITransport) {
    private class Root(messageProcessor: MessageProcessor) : ChannelOwner(messageProcessor, "", "")

    private val objects = hashMapOf<String, ChannelOwner>()
    private val callbacks = hashMapOf<Int, WaitResult<JsonElement>>()
    private val root = Root(this)
    private var lastId = 0

    fun registerObject(guid: String, obj: ChannelOwner) {
        objects[guid] = obj
    }

    fun unregisterObject(guid: String) {
        objects.remove(guid)
    }

    fun waitForObjectByGuid(guid: String): ChannelOwner? {
        while (!objects.containsKey(guid)) {
            processMessage()
        }
        return objects[guid]
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getExistingObject(guid: String): T =
        objects[guid] as T ?: throw PlaywrightException("Object doesn't exist: $guid")

    fun processMessage() {
        val messageStr = transport.pollMessage() ?: return
        val message = fromJson(messageStr, Message::class.java)
        dispatch(message)
    }

    private fun dispatch(message: Message) {
        if (message.id != 0) {
            val callback = callbacks[message.id]
            callback ?: throw PlaywrightException("Cannot find command to respond: ${message.id}")
            callbacks.remove(message.id)
            when {
                message.error == null -> callback.complete(message.result)
                message.error.error == null -> callback.completeWithException(PlaywrightException(message.error.toString()))
                message.error.error.name == "TimeoutError" -> callback.completeWithException(TimeoutException(message.error.error.toString()))
                else -> callback.completeWithException(DriverException(message.error.error))
            }
            return
        }

        message.method ?: return

        when (message.method) {
            CREATE.type -> {
                createRemoteObject(message.guid, message.params)
                return
            }
            DISPOSE.type -> {
                val obj =
                    objects[message.guid] ?: throw PlaywrightException("Cannot find object to dispose: ${message.guid}")
                obj.disconnect()
                return
            }
            else -> {
                val obj = objects[message.guid]
                    ?: throw PlaywrightException("Cannot find object to call ${message.method}: ${message.guid}")
                obj.handleEvent(message.method, message.params)
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
            BROWSER_CONTEXT.type -> BrowserContext(parent, type, guid, initializer)
            SELECTORS.type -> Selectors(parent, type, guid, initializer)
            REMOTE_BROWSER.type -> RemoteBrowser(parent, type, guid, initializer)
            else -> throw PlaywrightException("Unknown type $type")
        }
    }

    fun close() = transport.closeConnection()

    fun sendMessage(guid: String, method: String, params: JsonObject): JsonElement {
        return root.runUtil(sendMessageAsync(guid, method, params)) {}
    }

    private fun sendMessageAsync(guid: String, method: String, params: JsonObject): WaitResult<JsonElement> {
        val id = ++lastId
        val result = WaitResult<JsonElement>()
        callbacks[id] = result
        val message = JsonObject()
        message.addProperty("id", id)
        message.addProperty("guid", guid)
        message.addProperty("method", method)
        message.add("params", params)
        transport.sendMessage(toJson(message))
        return result
    }
}