package com.playwright.remote.engine.options.wait

import com.playwright.remote.engine.options.api.IBuilder


data class WaitForFrameSentOptions @JvmOverloads constructor(
    /**
     * Receives the {@code WebSocketFrame} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: WebSocketFramePredicate? = null,
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForFrameSentOptions>
) {
    init {
        builder.build(this)
    }
}
