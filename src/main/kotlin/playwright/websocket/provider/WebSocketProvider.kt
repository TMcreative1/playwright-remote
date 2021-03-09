package playwright.websocket.provider

import core.exceptions.WebSocketException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import playwright.websocket.listener.WebSocketListener
import java.time.Duration

import java.util.concurrent.TimeUnit

class WebSocketProvider(_webSocketListener: WebSocketListener) {
    private val client: OkHttpClient
    private val webSocketListener: WebSocketListener
    private val defaultTimeOut: Long = 3
    private lateinit var webSocket: WebSocket
    private val normalClosureStatus: Int = 1000

    init {
        this.client = OkHttpClient.Builder()
            .readTimeout(defaultTimeOut, TimeUnit.SECONDS)
            .build()
        this.webSocketListener = _webSocketListener
    }

    fun connect(url: String) {
        val request: Request = Request.Builder()
            .url(url)
            .build()

        try {
            webSocket = client.newWebSocket(request, this.webSocketListener)
        } catch (e: Exception) {
            throw WebSocketException("Connection failed!", e.cause)
        }
    }

    fun sendMessage(message: String) {
        this.webSocket.send(message)
    }

    fun pollMessage(timeout: Duration) : String? = webSocketListener.pollMessage(timeout.toMillis(), TimeUnit.MILLISECONDS)

    fun closeConnection() {
        this.webSocket.close(normalClosureStatus, "Close connection")
        this.client.dispatcher.executorService.shutdown()
    }
}