package playwright.websocket.provider

import core.exceptions.WebSocketException
import okhttp3.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocketProvider {
    private class CustomWebSocketListener : WebSocketListener() {
        private val incomingMessages: BlockingQueue<String> = LinkedBlockingQueue()
        lateinit var lastException: Exception

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            lastException = t as Exception
        }


        override fun onMessage(webSocket: WebSocket, text: String) {
            incomingMessages.add(text)
        }

        fun pollMessage(timeout: Long, timeUnit: TimeUnit): String? = incomingMessages.poll(timeout, timeUnit)
    }

    private val defaultTimeOut: Long = 3
    private var client = OkHttpClient.Builder()
        .readTimeout(defaultTimeOut, TimeUnit.SECONDS)
        .build()
    private lateinit var webSocket: WebSocket
    private val normalClosureStatus: Int = 1000
    private lateinit var webSocketListener: CustomWebSocketListener

    fun connect(url: String) {
        client = OkHttpClient.Builder()
            .readTimeout(defaultTimeOut, TimeUnit.SECONDS)
            .build()

        webSocketListener = CustomWebSocketListener()

        val request: Request = Request.Builder()
            .url(url)
            .build()

        try {
            webSocket = client.newWebSocket(request, webSocketListener)
        } catch (e: Exception) {
            throw WebSocketException("Connection failed!", webSocketListener.lastException)
        }
    }

    fun sendMessage(message: String) {
        this.webSocket.send(message)
    }

    fun pollMessage(timeout: Long = 100): String? =
        webSocketListener.pollMessage(timeout, TimeUnit.MILLISECONDS)

    fun closeConnection() {
        this.webSocket.close(normalClosureStatus, "Close connection")
        this.client.dispatcher.executorService.shutdown()
    }
}