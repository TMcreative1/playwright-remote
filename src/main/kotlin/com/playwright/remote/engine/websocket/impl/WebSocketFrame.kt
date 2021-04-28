package com.playwright.remote.engine.websocket.impl

import com.playwright.remote.engine.websocket.api.IWebSocketFrame
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

class WebSocketFrame(
    payload: String,
    isBase64: Boolean
) : IWebSocketFrame {
    private val bytes: ByteArray = if (isBase64) {
        Base64.getDecoder().decode(payload)
    } else {
        payload.toByteArray()
    }

    override fun binary(): ByteArray {
        return bytes
    }

    override fun text(): String {
        return String(bytes, UTF_8)
    }
}