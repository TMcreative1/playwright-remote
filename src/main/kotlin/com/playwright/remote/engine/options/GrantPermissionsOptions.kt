package com.playwright.remote.engine.options

class GrantPermissionsOptions(
    /**
     * The [origin] to grant permissions to, e.g. "https://example.com".
     */
    var origin: String? = null,
    fn: GrantPermissionsOptions.() -> Unit,
) {
    init {
        fn()
    }
}