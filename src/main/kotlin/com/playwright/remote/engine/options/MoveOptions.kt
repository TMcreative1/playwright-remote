package com.playwright.remote.engine.options

class MoveOptions(
    /**
     * defaults to 1. Sends intermediate {@code mousemove} events.
     */
    var steps: Int? = null,
    fn: MoveOptions.() -> Unit
) {
    init {
        fn()
    }
}