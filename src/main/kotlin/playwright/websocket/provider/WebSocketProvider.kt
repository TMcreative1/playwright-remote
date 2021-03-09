package playwright.websocket.provider

import core.exceptions.WebSocketException
import okhttp3.*
import java.time.Duration
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocketProvider {
    private class CustomWebSocketListener : WebSocketListener() {
        private val incomingMessages: BlockingQueue<String> = LinkedBlockingQueue()

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            throw WebSocketException("Websocket has been closed due to an error reading from or writing to the network!", t)
        }


        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            incomingMessages.add(text)
        }

        fun pollMessage(timeout: Long, timeUnit: TimeUnit): String? = incomingMessages.poll(timeout, timeUnit)
    }

    private val defaultTimeOut: Long = 3
    private lateinit var client: OkHttpClient
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
            throw WebSocketException("Connection failed!", e.cause)
        }
    }

    fun sendMessage(message: String) {
        this.webSocket.send(message)
    }

    fun pollMessage(timeout: Duration): String? =
        webSocketListener.pollMessage(timeout.toMillis(), TimeUnit.MILLISECONDS)

    fun closeConnection() {
        this.webSocket.close(normalClosureStatus, "Close connection")
        this.client.dispatcher.executorService.shutdown()
    }
}