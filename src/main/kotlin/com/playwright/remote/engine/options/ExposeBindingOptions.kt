package com.playwright.remote.engine.options

class ExposeBindingOptions(
    /**
     * Whether to pass the argument as a handle, instead of passing by value. When passing a handle, only one argument is
     * supported. When passing by value, multiple arguments are supported.
     */
    var handle: Boolean? = null,
    fn: ExposeBindingOptions.() -> Unit,
) {
    init {
        fn()
    }
}