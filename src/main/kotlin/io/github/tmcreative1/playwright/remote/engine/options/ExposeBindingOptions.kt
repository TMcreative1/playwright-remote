package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class ExposeBindingOptions @JvmOverloads constructor(
    /**
     * Whether to pass the argument as a handle, instead of passing by value. When passing a handle, only one argument is
     * supported. When passing by value, multiple arguments are supported.
     */
    var handle: Boolean? = null,
    @Transient private val builder: IBuilder<ExposeBindingOptions>
) {
    init {
        builder.build(this)
    }
}