package io.github.tmcreative1.playwright.remote.engine.options.wait

import io.github.tmcreative1.playwright.remote.engine.filechooser.api.IFileChooser
import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

typealias FileChooserPredicate = (IFileChooser) -> Boolean

data class WaitForFileChooserOptions @JvmOverloads constructor(
    /**
     * Receives the {@code FileChooser} object and resolves to truthy value when the waiting should resolve.
     */
    var predicate: FileChooserPredicate? = null,
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForFileChooserOptions>
) {
    init {
        builder.build(this)
    }
}
