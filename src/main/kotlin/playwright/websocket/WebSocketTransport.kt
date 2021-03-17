package playwright.websocket

import core.enums.EventType
import core.enums.EventType.CLOSE
import okhttp3.*
import playwright.listener.ListenerCollection
import playwright.listener.UniversalConsumer
import playwright.transport.ITransport
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class WebSocketTransport(url: String) : ITransport {
    private val incomingMessages: BlockingQueue<String> = LinkedBlockingQueue()
    private val lastException = Exception()
    private val listeners = ListenerCollection<EventType>()
    private val defaultTimeOut: Long = 3
    private val client = OkHttpClient.Builder()
        .readTimeout(defaultTimeOut, TimeUnit.SECONDS)
        .build()
    private val webSocket: WebSocket
    private val normalClosureStatus: Int = 1000
    private val webSocketListener = CustomWebSocketListener(incomingMessages, lastException)

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
        webSocket.send(message)
    }

    override fun pollMessage(timeout: Long, timeUnit: TimeUnit): String? =
        incomingMessages.poll(timeout, timeUnit)

    override fun closeConnection() {
        webSocket.close(normalClosureStatus, "Close connection")
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