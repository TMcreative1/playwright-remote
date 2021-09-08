package com.playwright.remote.engine.options.wait

import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.options.api.IBuilder

typealias ConsoleMessagePredicate = (IConsoleMessage) -> Boolean

data class WaitForConsoleMessageOptions @JvmOverloads constructor(
    /**
     * Receives the {@code ConsoleMessage} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: ConsoleMessagePredicate? = null,
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForConsoleMessageOptions>
) {
    init {
        builder.build(this)
    }
}
