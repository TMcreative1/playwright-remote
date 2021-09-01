package com.playwright.remote.engine.route.request

class Timing(
    /**
     * Request start time in milliseconds elapsed since January 1, 1970 00:00:00 UTC
     */
    var startTime: Double? = null,
    /**
     * Time immediately before the browser starts the domain name lookup for the resource. The value is given in milliseconds
     * relative to {@code startTime}, -1 if not available.
     */
    var domainLookupStart: Double? = null,
    /**
     * Time immediately after the browser starts the domain name lookup for the resource. The value is given in milliseconds
     * relative to {@code startTime}, -1 if not available.
     */
    var domainLookupEnd: Double? = null,
    /**
     * Time immediately before the user agent starts establishing the connection to the server to retrieve the resource. The
     * value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    var connectStart: Double? = null,
    /**
     * Time immediately before the browser starts the handshake process to secure the current connection. The value is given in
     * milliseconds relative to {@code startTime}, -1 if not available.
     */
    var secureConnectionStart: Double? = null,
    /**
     * Time immediately before the user agent starts establishing the connection to the server to retrieve the resource. The
     * value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    var connectEnd: Double? = null,
    /**
     * Time immediately before the browser starts requesting the resource from the server, cache, or local resource. The value
     * is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    var requestStart: Double? = null,
    /**
     * Time immediately after the browser starts requesting the resource from the server, cache, or local resource. The value
     * is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    var responseStart: Double? = null,
    /**
     * Time immediately after the browser receives the last byte of the resource or immediately before the transport connection
     * is closed, whichever comes first. The value is given in milliseconds relative to {@code startTime}, -1 if not available.
     */
    var responseEnd: Double? = null,
    fn: Timing.() -> Unit
) {
    init {
        fn()
    }
}