package playwright.websocket.handler

import playwright.websocket.Message
import playwright.websocket.parser.MessageParser

class MessageHandler {

    fun processMessage(message: String) {
        val messageObj: Message = MessageParser.parseMessage(message)
        println(messageObj.toString())
    }
}