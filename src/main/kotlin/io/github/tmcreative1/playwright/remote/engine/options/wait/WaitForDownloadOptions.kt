package com.playwright.remote.engine.options.wait

import com.playwright.remote.engine.download.api.IDownload
import com.playwright.remote.engine.options.api.IBuilder

typealias DownloadPredicate = (IDownload) -> Boolean

data class WaitForDownloadOptions @JvmOverloads constructor(
    /**
     * Receives the {@code Download} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: DownloadPredicate? = null,
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForDownloadOptions>
) {
    init {
        builder.build(this)
    }
}
