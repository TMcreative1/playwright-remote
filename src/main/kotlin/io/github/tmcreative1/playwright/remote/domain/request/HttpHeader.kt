package io.github.tmcreative1.playwright.remote.domain

data class HttpHeader(
    /**
     * Name of the header.
     */
    val name: String,
    /**
     * Value of the header.
     */
    val value: String
) {
}