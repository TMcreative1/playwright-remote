package com.playwright.remote.engine.websocket

import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.core.exceptions.WebSocketException
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.logger.CustomLogger
import com.playwright.remote.engine.transport.ITransport
import okhttp3.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocketTransport(url: String) : ITransport {
    private var isClosed: Array<Boolean?> = arrayOf(null)
    private val logger = CustomLogger()
    private val incomingMessages = LinkedBlockingQueue<String>()
    private val incomingErrors = ConcurrentHashMap<String, Exception>()
    private val lastException = Exception()
    private val listeners = ListenerCollection<EventType>()
    private val defaultTimeOut: Long = 3
    private val client = OkHttpClient.Builder()
        .readTimeout(defaultTimeOut, TimeUnit.SECONDS)
        .build()
    private val webSocket: WebSocket
    private val webSocketListener = CustomWebSocketListener(incomingMessages, lastException, incomingErrors, isClosed)

    private class CustomWebSocketListener(
        private val incomingMessages: BlockingQueue<String>,
        private var lastException: Exception,
        private val incomingErrors: ConcurrentHashMap<String, Exception>,
        private var isClosed: Array<Boolean?>
    ) :
        WebSocketListener() {
        private val logger = CustomLogger()

        override fun onOpen(webSocket: WebSocket, response: Response) {
            isClosed[0] = false
            logger.logInfo("Connection open")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            isClosed[0] = true
            logger.logInfo("Connection close")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            lastException = t as Exception
            incomingErrors["error"] = lastException
            logger.logInfo("RECEIVE error: $lastException")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            incomingMessages.add(text)
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

    override fun sendMessage(message: String) {
        webSocket.send(message)
    }

    override fun pollMessage(timeout: Long, timeUnit: TimeUnit): String? {
        checkIfClosed()
        return incomingMessages.poll(timeout, timeUnit)
    }

    override fun closeConnection() {
        webSocket.close(1000, "Normal Closure")
        client.dispatcher.executorService.shutdown()
        isClosed[0] = true
        logger.logInfo("Active connection count: ${client.connectionPool.connectionCount()}")
    }

    private fun checkIfClosed() {
        when {
            isClosed[0] != null && isClosed[0] == true -> {
                if (!incomingErrors.isEmpty()) {
                    throw WebSocketException(incomingErrors["error"]?.message, incomingErrors["error"]?.cause)
                } else {
                    throw WebSocketException("Connection was closed")
                }
            }
            isClosed[0] == null && !incomingErrors.isEmpty() -> throw WebSocketException(
                incomingErrors["error"]?.message,
                incomingErrors["error"]?.cause
            )
        }
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