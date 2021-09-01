package io.github.tmcreative1.playwright.remote.engine.options.wait

import io.github.tmcreative1.playwright.remote.engine.websocket.api.IWebSocketFrame
import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

typealias WebSocketFramePredicate = (IWebSocketFrame) -> Boolean

data class WaitForFrameReceivedOptions @JvmOverloads constructor(
    /**
     * Receives the {@code WebSocketFrame} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: WebSocketFramePredicate? = null,
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