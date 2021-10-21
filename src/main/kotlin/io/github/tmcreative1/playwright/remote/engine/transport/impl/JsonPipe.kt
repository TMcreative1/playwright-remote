package io.github.tmcreative1.playwright.remote.engine.transport.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection
import io.github.tmcreative1.playwright.remote.engine.listener.UniversalConsumer
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.transport.api.ITransport
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait
import java.time.Duration
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

class JsonPipe(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), ITransport {
    private val incoming: Queue<JsonObject> = LinkedList()
    private val listeners = ListenerCollection<EventType>()
    private var isClosed = false

    override fun sendMessage(message: JsonObject) {
        checkIfClosed()
        val params = JsonObject()
        params.add("message", message)
        sendMessage("send", params)
    }

    override fun pollMessage(timeout: Long, timeUnit: TimeUnit): JsonObject? {
        val duration = Duration.ofNanos(timeUnit.toNanos(timeout))
        val startTime = Instant.now()
        return runUtil(object : IWait<JsonObject?> {
            var message: JsonObject? = null
            override fun isFinished(): Boolean {
                if (incoming.isNotEmpty()) {
                    message = incoming.remove()
                    return true
                }
                checkIfClosed()
                if (Duration.between(startTime, Instant.now()) > duration) {
                    return true
                }
                return false
            }

            override fun get(): JsonObject? {
                return message
            }

            override fun dispose() {
            }
        }) {}
    }

    override fun close() {
        if (!isClosed) {
            sendMessage("close")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun onClose(handler: (JsonPipe) -> Unit) {
        listeners.add(EventType.CLOSE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    fun offClose(handler: (JsonPipe) -> Unit) {
        listeners.remove(EventType.CLOSE, handler as UniversalConsumer)
    }

    override fun handleEvent(event: String, params: JsonObject) {
        when (event) {
            "message" -> incoming.add(params["message"].asJsonObject)
            "closed" -> {
                isClosed = true
                listeners.notify(EventType.CLOSE, this)
            }
        }
    }

    private fun checkIfClosed() {
        if (isClosed) {
            throw PlaywrightException("Browser has been closed")
        }
    }
}