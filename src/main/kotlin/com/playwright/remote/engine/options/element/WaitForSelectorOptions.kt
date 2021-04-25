package com.playwright.remote.engine.options.element

import com.playwright.remote.core.enums.WaitForSelectorState
import com.playwright.remote.engine.options.api.IBuilder

data class WaitForSelectorOptions @JvmOverloads constructor(
    /**
     * Defaults to {@code "visible"}. Can be either:
     * <ul>
     * <li> {@code "attached"} - wait for element to be present in DOM.</li>
     * <li> {@code "detached"} - wait for element to not be present in DOM.</li>
     * <li> {@code "visible"} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element without any
     * content or with {@code display:none} has an empty bounding box and is not considered visible.</li>
     * <li> {@code "hidden"} - wait for element to be either detached from DOM, or have an empty bounding box or {@code visibility:hidden}. This
     * is opposite to the {@code "visible"} option.</li>
     * </ul>
     */
    var state: WaitForSelectorState? = null,
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<WaitForSelectorOptions>
) {
    init {
        builder.build(this)
    }
}
