package com.playwright.remote.engine.websocket.api

interface IWebSocketFrame {
    /**
     * Returns binary payload.
     */
    fun binary(): ByteArray

    /**
     * Returns text payload.
     */
    fun text(): String
}