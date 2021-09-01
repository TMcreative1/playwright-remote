package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class ResumeOptions @JvmOverloads constructor(
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
    @Transient private val builder: IBuilder<ResumeOptions>
) {
    init {
        builder.build(this)
    }
}