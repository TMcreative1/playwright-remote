package io.github.tmcreative1.playwright.remote.engine.route.request.api

import io.github.tmcreative1.playwright.remote.domain.request.HttpHeader
import io.github.tmcreative1.playwright.remote.domain.request.Sizes
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.route.request.Timing
import io.github.tmcreative1.playwright.remote.engine.route.response.api.IResponse

interface IRequest {
    /**
     * The method returns {@code null} unless this request has failed, as reported by {@code requestfailed} event.
     *
     * <p> Example of logging of all the failed requests:
     * <pre>{@code
     * page.onRequestFailed(request -> {
     *   System.out.println(request.url() + " " + request.failure());
     * });
     * }</pre>
     */
    fun failure(): String

    /**
     * Returns the {@code Frame} that initiated this request.
     */
    fun frame(): IFrame

    /**
     * **DEPRECATED** Incomplete list of headers as seen by the rendering engine. Use {@link IRequest#allHeaders
     * IRequest.allHeaders()} instead.
     */
    fun headers(): Map<String, String?>

    /**
     * Whether this request is driving frame's navigation.
     */
    fun isNavigationRequest(): Boolean

    /**
     * Request's method (GET, POST, etc.)
     */
    fun method(): String

    /**
     * Request's post body, if any.
     */
    fun postData(): String?

    /**
     * Request's post body in a binary form, if any.
     */
    fun postDataBuffer(): ByteArray?

    /**
     * Request that was redirected by the server to this one, if any.
     *
     * <p> When the server responds with a redirect, Playwright creates a new {@code Request} object. The two requests are connected by
     * {@code redirectedFrom()} and {@code redirectedTo()} methods. When multiple server redirects has happened, it is possible to
     * construct the whole redirect chain by repeatedly calling {@code redirectedFrom()}.
     *
     * <p> For example, if the website {@code http://example.com} redirects to {@code https://example.com}:
     * <pre>{@code
     * Response response = page.navigate("http://example.com");
     * System.out.println(response.request().redirectedFrom().url()); // "http://example.com"
     * }</pre>
     *
     * <p> If the website {@code https://google.com} has no redirects:
     * <pre>{@code
     * Response response = page.navigate("https://google.com");
     * System.out.println(response.request().redirectedFrom()); // null
     * }</pre>
     */
    fun redirectedFrom(): IRequest?

    /**
     * New request issued by the browser if the server responded with redirect.
     *
     * <p> This method is the opposite of {@link Request#redirectedFrom Request.redirectedFrom()}:
     * <pre>{@code
     * System.out.println(request.redirectedFrom().redirectedTo() == request); // true
     * }</pre>
     */
    fun redirectedTo(): IRequest?

    /**
     * Contains the request's resource type as it was perceived by the rendering engine. ResourceType will be one of the
     * following: {@code document}, {@code stylesheet}, {@code image}, {@code media}, {@code font}, {@code script}, {@code texttrack}, {@code xhr}, {@code fetch}, {@code eventsource},
     * {@code websocket}, {@code manifest}, {@code other}.
     */
    fun resourceType(): String

    /**
     * Returns the matching {@code Response} object, or {@code null} if the response was not received due to error.
     */
    fun response(): IResponse?

    /**
     * Returns resource timing information for given request. Most of the timing values become available upon the response,
     * {@code responseEnd} becomes available when request finishes. Find more information at <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/PerformanceResourceTiming">Resource Timing API</a>.
     * <pre>{@code
     * page.onRequestFinished(request -> {
     *   Timing timing = request.timing();
     *   System.out.println(timing.responseEnd - timing.startTime);
     * });
     * page.navigate("http://example.com");
     * }</pre>
     */
    fun timing(): Timing

    /**
     * URL of the request.
     */
    fun url(): String

    /**
     * An object with all the request HTTP headers associated with this request. The header names are lower-cased.
     */
    fun allHeaders(): Map<String, String?>

    /**
     * An array with all the request HTTP headers associated with this request. Unlike {@link Request#allHeaders
     * Request.allHeaders()}, header names are NOT lower-cased. Headers with multiple entries, such as {@code Set-Cookie}, appear in
     * the array multiple times.
     */
    fun headersArray(): List<HttpHeader>

    /**
     * Returns the value of the header matching the name. The name is case insensitive.
     *
     * @param name Name of the header.
     */
    fun headerValue(name: String): String?

    /**
     * Returns resource size information for given request.
     */
    fun sizes(): Sizes
}