package io.github.tmcreative1.playwright.remote.engine.options.wait

import io.github.tmcreative1.playwright.remote.core.enums.WaitUntilState
import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class WaitForNavigationOptions @JvmOverloads constructor(
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    var url: Any? = null,
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - consider operation to be finished when there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    var waitUntil: WaitUntilState? = null,
    @Transient private val builder: IBuilder<WaitForNavigationOptions>
) {
    init {
        builder.build(this)
    }
}
