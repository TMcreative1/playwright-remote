package com.playwright.remote.engine.route.response.api

import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.route.request.api.IRequest

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
    fun finished(): String?

    /**
     * Returns the {@code Frame} that initiated this response.
     */
    fun frame(): IFrame

    /**
     * Returns the object with HTTP headers associated with the response. All header names are lower-case.
     */
    fun headers(): Map<String, String>

    /**
     * Contains a boolean stating whether the response was successful (status in the range 200-299) or not.
     */
    fun ok(): Boolean

    /**
     * Returns the matching {@code Request} object.
     */
    fun request(): IRequest

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
}