package io.github.tmcreative1.playwright.remote.engine.transport.api

import com.google.gson.JsonObject
import java.util.concurrent.TimeUnit

interface ITransport {
    fun sendMessage(message: JsonObject)
    fun pollMessage(): JsonObject? = pollMessage(100)
    fun pollMessage(timeout: Long): JsonObject? = pollMessage(timeout, TimeUnit.MILLISECONDS)
    fun pollMessage(timeout: Long = 100, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): JsonObject?
    fun close()
}