package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class RegisterOptions @JvmOverloads constructor(
    /**
     * Whether to run this selector engine in isolated JavaScript environment. This environment has access to the same DOM, but
     * not any JavaScript objects from the frame's scripts. Defaults to {@code false}. Note that running as a content script is not
     * guaranteed when this engine is used together with other registered engines.
     */
    var contentScript: Boolean = false,
    @Transient private val builder: IBuilder<RegisterOptions>
) {

    init {
        builder.build(this)
    }
}