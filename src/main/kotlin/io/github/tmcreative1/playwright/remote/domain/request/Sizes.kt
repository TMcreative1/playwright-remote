package io.github.tmcreative1.playwright.remote.domain.request

data class Sizes(
    /**
     * Size of the request body (POST data payload) in bytes. Set to 0 if there was no body.
     */
    val requestBodySize: Int,
    /**
     * Total number of bytes from the start of the HTTP request message until (and including) the double CRLF before the body.
     */
    val requestHeadersSize: Int,
    /**
     * Size of the received response body (encoded) in bytes.
     */
    val responseBodySize: Int,
    /**
     * Total number of bytes from the start of the HTTP response message until (and including) the double CRLF before the body.
     */
    val responseHeadersSize: Int
)
