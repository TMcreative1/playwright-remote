package io.github.tmcreative1.playwright.remote.engine.options.wait

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class WaitForRequestOptions @JvmOverloads constructor(
    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can be
     * changed by using the {@link Page#setDefaultTimeout Page.setDefaultTimeout()} method.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForRequestOptions>
) {
    init {
        builder.build(this)
    }
}
