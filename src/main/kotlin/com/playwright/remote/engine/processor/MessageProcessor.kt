package com.playwright.remote.engine.processor

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.playwright.remote.core.enums.MethodType.CREATE
import com.playwright.remote.core.enums.MethodType.DISPOSE
import com.playwright.remote.core.enums.ObjectType.*
import com.playwright.remote.core.exceptions.DriverException
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.core.exceptions.TimeoutException
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.browser.impl.Browser
import com.playwright.remote.engine.browser.impl.BrowserContext
import com.playwright.remote.engine.browser.impl.Selectors
import com.playwright.remote.engine.console.impl.ConsoleMessage
import com.playwright.remote.engine.frame.impl.Frame
import com.playwright.remote.engine.page.impl.Page
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.parser.IParser.Companion.toJson
import com.playwright.remote.engine.route.request.impl.Request
import com.playwright.remote.engine.route.response.impl.Response
import com.playwright.remote.engine.transport.ITransport
import com.playwright.remote.engine.waits.impl.WaitResult
import com.playwright.remote.message.Message

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
            CONSOLE_MESSAGE.type -> ConsoleMessage(parent, type, guid, initializer)
            FRAME.type -> Frame(parent, type, guid, initializer)
            PAGE.type -> Page(parent, type, guid, initializer)
            SELECTORS.type -> Selectors(parent, type, guid, initializer)
            REMOTE_BROWSER.type -> RemoteBrowser(parent, type, guid, initializer)
            RESPONSE.type -> Response(parent, type, guid, initializer)
            REQUEST.type -> Request(parent, type, guid, initializer)
            else -> println("Uncomment it after implemented all types: currentType = $type")//TODO throw PlaywrightException("Unknown type $type")
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