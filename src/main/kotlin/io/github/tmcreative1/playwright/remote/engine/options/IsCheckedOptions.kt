package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class IsCheckedOptions @JvmOverloads constructor(
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<IsCheckedOptions>
) {
    init {
        builder.build(this)
    }
}
