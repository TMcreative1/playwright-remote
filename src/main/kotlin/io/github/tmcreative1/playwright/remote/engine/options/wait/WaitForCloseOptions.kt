package com.playwright.remote.engine.options.wait

import com.playwright.remote.engine.options.api.IBuilder

data class WaitForCloseOptions @JvmOverloads constructor(
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForCloseOptions>
) {
    init {
        builder.build(this)
    }
}
