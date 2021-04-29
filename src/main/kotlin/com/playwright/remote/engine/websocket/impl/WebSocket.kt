package com.playwright.remote.engine.websocket.impl

import com.google.gson.JsonObject
import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.*
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.options.wait.WaitForFrameReceivedOptions
import com.playwright.remote.engine.options.wait.WaitForFrameSentOptions
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.page.impl.Page
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.WaitEvent
import com.playwright.remote.engine.waits.impl.WaitRace
import com.playwright.remote.engine.waits.impl.WaitWebSocketClose
import com.playwright.remote.engine.waits.impl.WaitWebSocketError
import com.playwright.remote.engine.websocket.api.IWebSocket
import com.playwright.remote.engine.websocket.api.IWebSocketFrame

class WebSocket(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) : ChannelOwner(
    parent,
    type,
    guid,
    initializer
), IWebSocket {
    private val listeners = ListenerCollection<EventType>()
    private val page = parent as IPage
    private var isClosed = false

    @Suppress("UNCHECKED_CAST")
    override fun onClose(handler: (IWebSocket) -> Unit) {
        listeners.add(CLOSE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offClose(handler: (IWebSocket) -> Unit) {
        listeners.remove(CLOSE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onFrameReceived(handler: (IWebSocketFrame) -> Unit) {
        listeners.add(FRAMERECEIVED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offFrameReceived(handler: (IWebSocketFrame) -> Unit) {
        listeners.remove(FRAMERECEIVED, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onFrameSent(handler: (IWebSocketFrame) -> Unit) {
        listeners.add(FRAMESENT, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offFrameSent(handler: (IWebSocketFrame) -> Unit) {
        listeners.remove(FRAMESENT, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onSocketError(handler: (String) -> Unit) {
        listeners.add(SOCKETERROR, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun offSocketError(handler: (String) -> Unit) {
        listeners.remove(SOCKETERROR, handler as UniversalConsumer)
    }

    override fun isClosed(): Boolean {
        return isClosed
    }

    override fun url(): String {
        return initializer["url"].asString
    }

    override fun waitForFrameReceived(options: WaitForFrameReceivedOptions?, callback: () -> Unit): IWebSocketFrame {
        return waitForEventWithTimeout(
            FRAMERECEIVED,
            if (options == null) WaitForFrameReceivedOptions {}.timeout else options.timeout,
            callback
        )
    }

    override fun waitForFrameSent(options: WaitForFrameSentOptions?, callback: () -> Unit): IWebSocketFrame {
        return waitForEventWithTimeout(
            FRAMESENT,
            if (options == null) WaitForFrameSentOptions {}.timeout else options.timeout,
            callback
        )
    }

    private fun <T> waitForEventWithTimeout(eventType: EventType, timeout: Double?, code: () -> Unit): T {
        val waitList = arrayListOf<IWait<T>>()
        waitList.add(WaitEvent(listeners, eventType))
        waitList.add(WaitWebSocketClose(listeners))
        waitList.add(WaitWebSocketError(listeners))
        waitList.add((page as Page).createWaitForCloseHelper())
        waitList.add(page.createWaitTimeout(timeout))
        return runUtil(WaitRace(waitList), code)
    }

    override fun handleEvent(event: String, params: JsonObject) {
        when (event) {
            "frameSent" -> {
                val webSockedFrame = WebSocketFrame(
                    params["data"].asString, params["opcode"].asInt == 2
                )
                listeners.notify(FRAMESENT, webSockedFrame)
            }
            "frameReceived" -> {
                val webSocketFrame = WebSocketFrame(
                    params["data"].asString, params["opcode"].asInt == 2
                )
                listeners.notify(FRAMERECEIVED, webSocketFrame)
            }
            "socketError" -> {
                val error = params["error"].asString
                listeners.notify(SOCKETERROR, error)
            }
            "close" -> {
                isClosed = true
                listeners.notify(CLOSE, this)
            }
            else -> throw PlaywrightException("Unknown event: $event")
        }
    }
}