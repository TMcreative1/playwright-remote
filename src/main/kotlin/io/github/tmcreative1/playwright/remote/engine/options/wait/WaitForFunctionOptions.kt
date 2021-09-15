package io.github.tmcreative1.playwright.remote.engine.options.wait

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class WaitForFunctionOptions @JvmOverloads constructor(
    /**
     * If specified, then it is treated as an interval in milliseconds at which the function would be executed. By default if
     * the option is not specified {@code expression} is executed in {@code requestAnimationFrame} callback.
     */
    var pollingInterval: Double? = null,
    /**
     * maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForFunctionOptions>
) {
    init {
        builder.build(this)
    }
}
