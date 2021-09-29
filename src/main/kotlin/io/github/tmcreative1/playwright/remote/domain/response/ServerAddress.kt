package io.github.tmcreative1.playwright.remote.domain.response

data class ServerAddress(
    /**
     * IPv4 or IPV6 address of the server.
     */
    val ipAddress: String,
    val port: Int
)
