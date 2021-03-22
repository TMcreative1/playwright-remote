package com.playwright.remote.engine.options

import com.playwright.remote.engine.page.api.IPage
import java.util.function.Predicate

class WaitForPageOptions(
    /**
     * Receives the {@code Page} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: Predicate<IPage>? = null,
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    fn: WaitForPageOptions.() -> Unit
) {
    init {
        fn()
    }
}