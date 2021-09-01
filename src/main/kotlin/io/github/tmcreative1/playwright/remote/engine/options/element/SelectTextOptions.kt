package io.github.tmcreative1.playwright.remote.engine.options.element

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class SelectTextOptions @JvmOverloads constructor(
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<SelectTextOptions>
) {
    init {
        builder.build(this)
    }
}
