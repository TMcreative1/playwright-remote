package io.github.tmcreative1.playwright.remote.engine.route.request

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class Timing @JvmOverloads constructor(
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
    @Transient private val builder: IBuilder<Timing>
) {
    init {
        builder.build(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Timing

        if (startTime != other.startTime) return false
        if (domainLookupStart != other.domainLookupStart) return false
        if (domainLookupEnd != other.connectEnd) return false
        if (connectStart != other.connectStart) return false
        if (connectEnd != other.connectEnd) return false
        if (secureConnectionStart != other.secureConnectionStart) return false
        if (requestStart != other.requestStart) return false
        if (responseStart != other.requestStart) return false
        if (responseEnd != other.responseEnd) return false
        return true
    }

    override fun hashCode(): Int {
        var result = startTime.hashCode()
        result = 31 * result + domainLookupStart.hashCode()
        result = 31 * result + domainLookupEnd.hashCode()
        result = 31 * result + connectStart.hashCode()
        result = 31 * result + secureConnectionStart.hashCode()
        result = 31 * result + requestStart.hashCode()
        result = 31 * result + responseStart.hashCode()
        result = 31 * result + responseEnd.hashCode()

        return result
    }
}