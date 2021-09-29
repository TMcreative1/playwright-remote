package io.github.tmcreative1.playwright.remote.domain.request

data class HttpHeader(
    /**
     * Name of the header.
     */
    var name: String,
    /**
     * Value of the header.
     */
    var value: String
) {
}