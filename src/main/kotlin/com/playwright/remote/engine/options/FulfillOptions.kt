package com.playwright.remote.engine.options

import java.nio.file.Path

class FulfillOptions(
    /**
     * Optional response body as text.
     */
    var body: String? = null,
    /**
     * Optional response body as raw bytes.
     */
    var bodyBytes: ByteArray? = null,
    /**
     * If set, equals to setting {@code Content-Type} response header.
     */
    var contentType: String? = null,
    /**
     * Response headers. Header values will be converted to a string.
     */
    var headers: Map<String, String>? = null,
    /**
     * File path to respond with. The content type will be inferred from file extension. If {@code path} is a relative path, then it
     * is resolved relative to the current working directory.
     */
    var path: Path? = null,
    /**
     * Response status code, defaults to {@code 200}.
     */
    var status: Int? = null,
    fn: FulfillOptions.() -> Unit
) {
    init {
        fn()
    }
}