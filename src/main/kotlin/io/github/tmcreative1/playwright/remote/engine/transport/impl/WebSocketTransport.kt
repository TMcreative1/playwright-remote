package io.github.tmcreative1.playwright.remote.engine.transport.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.core.enums.EventType.CLOSE
import io.github.tmcreative1.playwright.remote.core.exceptions.WebSocketException
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection
import io.github.tmcreative1.playwright.remote.engine.listener.UniversalConsumer
import io.github.tmcreative1.playwright.remote.engine.logger.CustomLogger
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.fromJson
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.toJson
import io.github.tmcreative1.playwright.remote.engine.transport.api.ITransport
import okhttp3.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocketTransport(url: String) : ITransport {
    private val logger = CustomLogger()
    private val incomingMessages = LinkedBlockingQueue<JsonObject>()
    private val incomingErrors = ConcurrentHashMap<String, Exception>()
    private val lastException = Exception()
    private val listeners = ListenerCollection<EventType>()
    private val defaultTimeOut: Long = 3
    private val client = OkHttpClient.Builder()
        .readTimeout(defaultTimeOut, TimeUnit.SECONDS)
        .build()
    private val webSocket: WebSocket
    private val webSocketListener = CustomWebSocketListener(incomingMessages, lastException, incomingErrors)

    private class CustomWebSocketListener(
        private val incomingMessages: BlockingQueue<JsonObject>,
        private var lastException: Exception,
        private val incomingErrors: ConcurrentHashMap<String, Exception>
    ) :
        WebSocketListener() {
        private val logger = CustomLogger()
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            lastException = t as Exception
            incomingErrors["error"] = lastException
            logger.logInfo("RECEIVE error: $lastException")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            incomingMessages.add(fromJson(text, JsonObject::class.java))
            logger.logReceiveMessage(text)
        }
    }

    init {
        val request: Request
        try {
            request = Request.Builder()
                .url(url)
                .build()
        } catch (e: IllegalArgumentException) {
            throw WebSocketException("Inappropriate url: ${e.message}", e.cause)
        }

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    override fun sendMessage(message: JsonObject) {
        webSocket.send(toJson(message))
    }

    override fun pollMessage(timeout: Long, timeUnit: TimeUnit): JsonObject? {
        if (!incomingErrors.isEmpty()) {
            close()
            throw WebSocketException(incomingErrors["error"]?.message, incomingErrors["error"]?.cause)
        }
        return incomingMessages.poll(timeout, timeUnit)
    }

    override fun close() {
        webSocket.close(1000, "Normal Closure")
        client.dispatcher.executorService.shutdown()
        logger.logInfo("Active connection count: ${client.connectionPool.connectionCount()}")
    }
}