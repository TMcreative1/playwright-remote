package io.github.tmcreative1.playwright.remote.engine.transport

import java.util.concurrent.TimeUnit

interface ITransport {
    fun sendMessage(message: String)
    fun pollMessage(): String? = pollMessage(100)
    fun pollMessage(timeout: Long): String? = pollMessage(timeout, TimeUnit.MILLISECONDS)
    fun pollMessage(timeout: Long = 100, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): String?
    fun closeConnection()
}