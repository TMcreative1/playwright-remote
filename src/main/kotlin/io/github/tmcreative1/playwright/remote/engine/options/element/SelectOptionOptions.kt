package io.github.tmcreative1.playwright.remote.engine.options.element

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class SelectOptionOptions @JvmOverloads constructor(
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    var noWaitAfter: Boolean? = null,
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<SelectOptionOptions>
) {
    init {
        builder.build(this)
    }
}
