import playwright.websocket.handler.MessageHandler
import playwright.websocket.provider.WebSocketProvider

fun main(args: Array<String>) {
    val webSocketProvider = WebSocketProvider()
    webSocketProvider.connect("ws://127.0.0.1:4444/a3b02e1f28fdceb74ec3688eec70d626")
    val messageHandler = MessageHandler()
    val message: String? = webSocketProvider.pollMessage()
    messageHandler.processMessage(message!!)
    webSocketProvider.closeConnection()
}