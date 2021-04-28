package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder
import com.playwright.remote.engine.websocket.api.IWebSocketFrame

data class WaitForFrameReceivedOptions @JvmOverloads constructor(
    /**
     * Receives the {@code WebSocketFrame} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: ((IWebSocketFrame) -> Boolean)? = null,
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForFrameReceivedOptions>
) {
    init {
        builder.build(this)
    }
}