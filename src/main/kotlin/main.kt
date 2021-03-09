import playwright.websocket.handler.MessageHandler
import playwright.websocket.listener.WebSocketListener
import playwright.websocket.provider.WebSocketProvider
import java.time.Duration

fun main(args: Array<String>) {
    val webSocketListener = WebSocketListener()
    val webSocketProvider = WebSocketProvider(webSocketListener)
    webSocketProvider.connect("ws://127.0.0.1:4444/a3b02e1f28fdceb74ec3688eec70d626")
    val messageHandler: MessageHandler = MessageHandler()
    val message: String? = webSocketProvider.pollMessage(Duration.ofSeconds(5))
    messageHandler.processMessage(message!!)
    webSocketProvider.closeConnection()
}