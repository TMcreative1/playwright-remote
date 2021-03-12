package playwright.parser

import com.google.gson.Gson
import playwright.websocket.Message

class MessageParser {
    companion object {
        @JvmStatic
        fun parseMessage(message: String): Message = Gson().fromJson(message, Message::class.java)
    }
}