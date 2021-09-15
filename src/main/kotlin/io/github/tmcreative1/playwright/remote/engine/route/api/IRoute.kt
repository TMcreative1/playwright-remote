package io.github.tmcreative1.playwright.remote.engine.route.api

import io.github.tmcreative1.playwright.remote.engine.options.FulfillOptions
import io.github.tmcreative1.playwright.remote.engine.options.ResumeOptions
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest

interface IRoute {
    /**
     * Aborts the route's request.
     *
     * @param errorCode Optional error code. Defaults to {@code failed}, could be one of the following:
     * <ul>
     * <li> {@code "aborted"} - An operation was aborted (due to user action)</li>
     * <li> {@code "accessdenied"} - Permission to access a resource, other than the network, was denied</li>
     * <li> {@code "addressunreachable"} - The IP address is unreachable. This usually means that there is no route to the specified host
     * or network.</li>
     * <li> {@code "blockedbyclient"} - The client chose to block the request.</li>
     * <li> {@code "blockedbyresponse"} - The request failed because the response was delivered along with requirements which are not met
     * ('X-Frame-Options' and 'Content-Security-Policy' ancestor checks, for instance).</li>
     * <li> {@code "connectionaborted"} - A connection timed out as a result of not receiving an ACK for data sent.</li>
     * <li> {@code "connectionclosed"} - A connection was closed (corresponding to a TCP FIN).</li>
     * <li> {@code "connectionfailed"} - A connection attempt failed.</li>
     * <li> {@code "connectionrefused"} - A connection attempt was refused.</li>
     * <li> {@code "connectionreset"} - A connection was reset (corresponding to a TCP RST).</li>
     * <li> {@code "internetdisconnected"} - The Internet connection has been lost.</li>
     * <li> {@code "namenotresolved"} - The host name could not be resolved.</li>
     * <li> {@code "timedout"} - An operation timed out.</li>
     * <li> {@code "failed"} - A generic failure occurred.</li>
     * </ul>
     */
    fun abort() = abort(null)

    /**
     * Aborts the route's request.
     *
     * @param errorCode Optional error code. Defaults to {@code failed}, could be one of the following:
     * <ul>
     * <li> {@code "aborted"} - An operation was aborted (due to user action)</li>
     * <li> {@code "accessdenied"} - Permission to access a resource, other than the network, was denied</li>
     * <li> {@code "addressunreachable"} - The IP address is unreachable. This usually means that there is no route to the specified host
     * or network.</li>
     * <li> {@code "blockedbyclient"} - The client chose to block the request.</li>
     * <li> {@code "blockedbyresponse"} - The request failed because the response was delivered along with requirements which are not met
     * ('X-Frame-Options' and 'Content-Security-Policy' ancestor checks, for instance).</li>
     * <li> {@code "connectionaborted"} - A connection timed out as a result of not receiving an ACK for data sent.</li>
     * <li> {@code "connectionclosed"} - A connection was closed (corresponding to a TCP FIN).</li>
     * <li> {@code "connectionfailed"} - A connection attempt failed.</li>
     * <li> {@code "connectionrefused"} - A connection attempt was refused.</li>
     * <li> {@code "connectionreset"} - A connection was reset (corresponding to a TCP RST).</li>
     * <li> {@code "internetdisconnected"} - The Internet connection has been lost.</li>
     * <li> {@code "namenotresolved"} - The host name could not be resolved.</li>
     * <li> {@code "timedout"} - An operation timed out.</li>
     * <li> {@code "failed"} - A generic failure occurred.</li>
     * </ul>
     */
    fun abort(errorCode: String?)

    /**
     * Continues route's request with optional overrides.
     * <pre>{@code
     * page.route("**\/'*", route -> {
     *   // Override headers
     *   Map<String, String> headers = new HashMap<>(route.request().headers());
     *   headers.put("foo", "bar"); // set "foo" header
     *   headers.remove("origin"); // remove "origin" header
     *   route.resume(new Route.ResumeOptions().setHeaders(headers));
     * });
     * }</pre>
     */
    fun resume() = resume(null)

    /**
     * Continues route's request with optional overrides.
     * <pre>{@code
     * page.route("**\/'*", route -> {
     *   // Override headers
     *   Map<String, String> headers = new HashMap<>(route.request().headers());
     *   headers.put("foo", "bar"); // set "foo" header
     *   headers.remove("origin"); // remove "origin" header
     *   route.resume(new Route.ResumeOptions().setHeaders(headers));
     * });
     * }</pre>
     */
    fun resume(options: ResumeOptions?)

    /**
     * Fulfills route's request with given response.
     *
     * <p> An example of fulfilling all requests with 404 responses:
     * <pre>{@code
     * page.route("**\/'*", route -> {
     *   route.fulfill(new Route.FulfillOptions()
     *     .setStatus(404)
     *     .setContentType("text/plain")
     *     .setBody("Not Found!"));
     * });
     * }</pre>
     *
     * <p> An example of serving static file:
     * <pre>{@code
     * page.route("**\/xhr_endpoint", route -> route.fulfill(
     *   new Route.FulfillOptions().setPath(Paths.get("mock_data.json")));
     * }</pre>
     */
    fun fulfill() = fulfill(null)

    /**
     * Fulfills route's request with given response.
     *
     * <p> An example of fulfilling all requests with 404 responses:
     * <pre>{@code
     * page.route("**\/'*", route -> {
     *   route.fulfill(new Route.FulfillOptions()
     *     .setStatus(404)
     *     .setContentType("text/plain")
     *     .setBody("Not Found!"));
     * });
     * }</pre>
     *
     * <p> An example of serving static file:
     * <pre>{@code
     * page.route("**\/xhr_endpoint", route -> route.fulfill(
     *   new Route.FulfillOptions().setPath(Paths.get("mock_data.json")));
     * }</pre>
     */
    fun fulfill(options: FulfillOptions?)

    /**
     * A request to be routed.
     */
    fun request(): IRequest
}