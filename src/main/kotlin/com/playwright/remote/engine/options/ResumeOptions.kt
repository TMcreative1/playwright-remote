package com.playwright.remote.engine.options

class ResumeOptions(
    /**
     * If set changes the request HTTP headers. Header values will be converted to a string.
     */
    var headers: Map<String, String>? = null,
    /**
     * If set changes the request method (e.g. GET or POST)
     */
    var method: String? = null,
    /**
     * If set changes the post data of request
     */
    var postData: Any? = null,
    /**
     * If set changes the request URL. New URL must have same protocol as original one.
     */
    var url: String? = null,
    fn: ResumeOptions.() -> Unit
) {
    init {
        fn()
    }
}