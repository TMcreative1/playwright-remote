package com.playwright.remote.engine.options.wait

import com.playwright.remote.engine.options.api.IBuilder

data class WaitForLoadStateOptions @JvmOverloads constructor(
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForLoadStateOptions>
) {
    init {
        builder.build(this)
    }
}
