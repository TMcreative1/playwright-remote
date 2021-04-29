package com.playwright.remote.engine.options.wait

import com.playwright.remote.core.enums.WaitUntilState
import com.playwright.remote.engine.options.api.IBuilder

data class WaitForURLOptions @JvmOverloads constructor(
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - consider operation to be finished when there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    var waitUntil: WaitUntilState,
    @Transient private val builder: IBuilder<WaitForURLOptions>
) {
    init {
        builder.build(this)
    }
}
