package com.playwright.remote.engine.logger

class CustomLogger {
    fun logReceiveMessage(message: String) {
        println("RECEIVE message: $message")
    }

    fun logSendMessage(message: String) {
        println("SEND message: $message")
    }

    fun logInfo(info: String) {
        println(info)
    }
}