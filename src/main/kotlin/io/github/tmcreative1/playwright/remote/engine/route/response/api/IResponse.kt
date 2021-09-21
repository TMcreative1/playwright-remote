package io.github.tmcreative1.playwright.remote.engine.route.response.api

import io.github.tmcreative1.playwright.remote.domain.request.HttpHeader
import io.github.tmcreative1.playwright.remote.domain.response.SecurityDetails
import io.github.tmcreative1.playwright.remote.domain.response.ServerAddress
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest

/**
 * {@code Response} class represents responses which are received by page.
 */
interface IResponse {
    /**
     * Returns the buffer with response body.
     */
    fun body(): ByteArray

    /**
     * Waits for this response to finish, returns failure error if request failed.
     */
    fun finished(): String

    /**
     * Returns the {@code Frame} that initiated this response.
     */
    fun frame(): IFrame

    /**
     * **DEPRECATED** Incomplete list of headers as seen by the rendering engine. Use {@link IResponse#allHeaders
     * Response.allHeaders()} instead.
     */
    fun headers(): Map<String, String?>

    /**
     * Contains a boolean stating whether the response was successful (status in the range 200-299) or not.
     */
    fun ok(): Boolean

    /**
     * Returns the matching {@code Request} object.
     */
    fun request(): IRequest

    /**
     * Returns SSL and other security information.
     */
    fun securityDetails(): SecurityDetails?

    /**
     * Returns the IP address and port of the server.
     */
    fun serverAddress(): ServerAddress?

    /**
     * Contains the status code of the response (e.g., 200 for a success).
     */
    fun status(): Int

    /**
     * Contains the status text of the response (e.g. usually an "OK" for a success).
     */
    fun statusText(): String

    /**
     * Returns the text representation of response body.
     */
    fun text(): String

    /**
     * Contains the URL of the response.
     */
    fun url(): String

    /**
     * An object with all the response HTTP headers associated with this response.
     */
    fun allHeaders(): Map<String, String?>

    /**
     * An array with all the request HTTP headers associated with this response. Unlike {@link Response#allHeaders
     * Response.allHeaders()}, header names are NOT lower-cased. Headers with multiple entries, such as {@code Set-Cookie}, appear in
     * the array multiple times.
     */
    fun headersArray(): List<HttpHeader>

    /**
     * Returns the value of the header matching the name. The name is case insensitive. If multiple headers have the same name
     * (except {@code set-cookie}), they are returned as a list separated by {@code , }. For {@code set-cookie}, the {@code \n} separator is used. If
     * no headers are found, {@code null} is returned.
     *
     * @param name Name of the header.
     */
    fun headerValue(name: String): String?

    /**
     * Returns all values of the headers matching the name, for example {@code set-cookie}. The name is case insensitive.
     *
     * @param name Name of the header.
     */
    fun headerValues(name: String): List<String>?
}