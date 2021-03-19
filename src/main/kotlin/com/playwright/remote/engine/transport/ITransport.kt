package com.playwright.remote.engine.transport

import java.util.concurrent.TimeUnit

interface ITransport {
    fun sendMessage(message: String)
    fun pollMessage(timeout: Long = 100, timeUnit: TimeUnit = TimeUnit.MILLISECONDS): String?
    fun closeConnection()
}