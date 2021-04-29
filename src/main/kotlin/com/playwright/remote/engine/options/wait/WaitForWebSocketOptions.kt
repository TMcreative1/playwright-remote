package com.playwright.remote.engine.options.wait

import com.playwright.remote.engine.options.api.IBuilder
import com.playwright.remote.engine.websocket.api.IWebSocket

typealias WebSocketPredicate = (IWebSocket) -> Boolean

data class WaitForWebSocketOptions @JvmOverloads constructor(
    /**
     * Receives the {@code WebSocket} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: WebSocketPredicate? = null,
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForWebSocketOptions>
) {
    init {
        builder.build(this)
    }
}
