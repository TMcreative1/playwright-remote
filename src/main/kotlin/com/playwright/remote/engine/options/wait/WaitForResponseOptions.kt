package com.playwright.remote.engine.options.wait

import com.playwright.remote.engine.options.api.IBuilder

data class WaitForResponseOptions @JvmOverloads constructor(
    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can be
     * changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForResponseOptions>
) {
    init {
        builder.build(this)
    }
}
