package com.playwright.remote.engine.logger

import com.playwright.remote.domain.message.Message
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.parser.IParser.Companion.toJson

class CustomLogger {
    fun logReceiveMessage(message: String) {
        println("RECEIVE message: ${toJson(fromJson(message, Message::class.java))}")
    }

    fun logSendMessage(message: String) {
        println("SEND message: $message")
    }

    fun logInfo(info: String) {
        println(info)
    }
}