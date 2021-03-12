package playwright.websocket

import okhttp3.*
import playwright.transport.ITransport
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocketTransport(url: String) : ITransport {
    private val incomingMessages: BlockingQueue<String> = LinkedBlockingQueue()
    private var lastException = Exception()
    private val defaultTimeOut: Long = 3
    private val client = OkHttpClient.Builder()
        .readTimeout(defaultTimeOut, TimeUnit.SECONDS)
        .build()
    private lateinit var webSocket: WebSocket
    private val normalClosureStatus: Int = 1000
    private var webSocketListener = CustomWebSocketListener(incomingMessages, lastException)

    private class CustomWebSocketListener(val incomingMessages: BlockingQueue<String>, var lastException: Exception) :
        WebSocketListener() {
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            lastException = t as Exception
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            incomingMessages.add(text)
        }
    }

    init {
        val request: Request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, webSocketListener)
    }

    override fun sendMessage(message: String) {
        this.webSocket.send(message)
    }

    override fun pollMessage(timeout: Long, timeUnit: TimeUnit): String? =
        incomingMessages.poll(timeout, timeUnit)

    override fun closeConnection() {
        this.webSocket.close(normalClosureStatus, "Close connection")
        this.client.dispatcher.executorService.shutdown()
    }
}