package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest

typealias RequestPredicate = (IRequest) -> Boolean

data class WaitForRequestFinishedOptions @JvmOverloads constructor(
    /**
     * Receives the {@code Request} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: RequestPredicate? = null,
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForRequestFinishedOptions>
) {
    init {
        builder.build(this)
    }
}
