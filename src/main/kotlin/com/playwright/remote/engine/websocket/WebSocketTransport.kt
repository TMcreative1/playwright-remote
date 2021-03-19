package com.playwright.remote.playwright.websocket

import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.core.exceptions.WebSocketException
import com.playwright.remote.playwright.listener.ListenerCollection
import com.playwright.remote.playwright.listener.UniversalConsumer
import com.playwright.remote.playwright.transport.ITransport
import okhttp3.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocketTransport(url: String) : ITransport {
    private val incomingMessages = LinkedBlockingQueue<String>()
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
        val incomingMessages: BlockingQueue<String>,
        var lastException: Exception,
        val incomingErrors: ConcurrentHashMap<String, Exception>
    ) :
        WebSocketListener() {
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            lastException = t as Exception
            incomingErrors["error"] = lastException
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            incomingMessages.add(text)
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

    override fun sendMessage(message: String) {
        webSocket.send(message)
    }

    override fun pollMessage(timeout: Long, timeUnit: TimeUnit): String? {
        if (!incomingErrors.isEmpty()) {
            closeConnection()
            throw WebSocketException(incomingErrors["error"]?.message, incomingErrors["error"]?.cause)
        }
        return incomingMessages.poll(timeout, timeUnit)
    }

    override fun closeConnection() {
        client.dispatcher.executorService.shutdown()
    }

    @Suppress("UNCHECKED_CAST")
    fun onClose(handler: (WebSocketTransport) -> Unit) {
        listeners.add(CLOSE, handler as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    fun offClose(handler: (WebSocketTransport) -> Unit) {
        listeners.remove(CLOSE, handler as UniversalConsumer)
    }
}