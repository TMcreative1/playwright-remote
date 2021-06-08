package com.playwright.remote.engine.logger

class CustomLogger {
    companion object {
        private val LOG_ENABLED = System.getenv("DEBUG") != null
    }

    fun logReceiveMessage(message: String) {
        if (LOG_ENABLED) {
            println("RECEIVE message: $message")
        }
    }

    fun logSendMessage(message: String) {
        if (LOG_ENABLED) {
            println("SEND message: $message")
        }
    }

    fun logInfo(info: String) {
        if (LOG_ENABLED) {
            println(info)
        }
    }
}