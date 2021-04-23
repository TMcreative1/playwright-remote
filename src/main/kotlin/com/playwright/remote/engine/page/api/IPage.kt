package com.playwright.remote.engine.page.api

import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.dialog.api.IDialog
import com.playwright.remote.engine.download.api.IDownload
import com.playwright.remote.engine.options.NavigateOptions
import com.playwright.remote.engine.route.response.api.IResponse

interface IPage {
    /**
     * Emitted when the page closes.
     */
    fun onClose(handler: (IPage) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onClose onClose(handler)}.
     */
    fun offClose(handler: (IPage) -> Unit)

    /**
     * Emitted when JavaScript within the page calls one of console API methods, e.g. {@code console.log} or {@code console.dir}. Also
     * emitted if the page throws an error or a warning.
     *
     * <p> The arguments passed into {@code console.log} appear as arguments on the event handler.
     *
     * <p> An example of handling {@code console} event:
     * <pre>{@code
     * page.onConsole(msg -> {
     *   for (int i = 0; i < msg.args().size(); ++i)
     *     System.out.println(i + ": " + msg.args().get(i).jsonValue());
     * });
     * page.evaluate("() => console.log('hello', 5, {foo: 'bar'})");
     * }</pre>
     */
    fun onConsoleMessage(handler: (IConsoleMessage) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onConsoleMessage onConsoleMessage(handler)}.
     */
    fun offConsoleMessage(handler: (IConsoleMessage) -> Unit)

    /**
     * Emitted when the page crashes. Browser pages might crash if they try to allocate too much memory. When the page crashes,
     * ongoing and subsequent operations will throw.
     *
     * <p> The most common way to deal with crashes is to catch an exception:
     * <pre>{@code
     * try {
     *   // Crash might happen during a click.
     *   page.click("button");
     *   // Or while waiting for an event.
     *   page.waitForPopup(() -> {});
     * } catch (PlaywrightException e) {
     *   // When the page crashes, exception message contains "crash".
     * }
     * }</pre>
     */
    fun onCrash(handler: (IPage) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onCrash onCrash(handler)}.
     */
    fun offCrash(handler: (IPage) -> Unit)

    /**
     * Emitted when a JavaScript dialog appears, such as {@code alert}, {@code prompt}, {@code confirm} or {@code beforeunload}. Listener **must**
     * either {@link Dialog#accept Dialog.accept()} or {@link Dialog#dismiss Dialog.dismiss()} the dialog - otherwise the page
     * will <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/EventLoop#never_blocking">freeze</a> waiting for
     * the dialog, and actions like click will never finish.
     *
     * <p> <strong>NOTE:</strong> When no {@link Page#onDialog Page.onDialog()} listeners are present, all dialogs are automatically dismissed.
     */
    fun onDialog(handler: (IDialog) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onDialog onDialog(handler)}.
     */
    fun offDialog(handler: (IDialog) -> Unit)

    /**
     * Emitted when the JavaScript <a
     * href="https://developer.mozilla.org/en-US/docs/Web/Events/DOMContentLoaded">{@code DOMContentLoaded}</a> event is dispatched.
     */
    fun onDomContentLoaded(handler: (IPage) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onDOMContentLoaded onDOMContentLoaded(handler)}.
     */
    fun offDomContentLoaded(handler: (IPage) -> Unit)

    /**
     * Emitted when attachment download started. User can access basic file operations on downloaded content via the passed
     * {@code Download} instance.
     *
     * <p> <strong>NOTE:</strong> Browser context **must** be created with the {@code acceptDownloads} set to {@code true} when user needs access to the downloaded
     * content. If {@code acceptDownloads} is not set, download events are emitted, but the actual download is not performed and user
     * has no access to the downloaded files.
     */
    fun onDownload(handler: (IDownload) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onDownload onDownload(handler)}.
     */
    fun offDownload(handler: (IDownload) -> Unit)

    /**
     * Get the browser context that the page belongs to.
     */
    fun context(): IBrowserContext

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect.
     *
     * <p> {@code page.goto} will throw an error if:
     * <ul>
     * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
     * <li> target URL is invalid.</li>
     * <li> the {@code timeout} is exceeded during navigation.</li>
     * <li> the remote server does not respond or is unreachable.</li>
     * <li> the main resource failed to load.</li>
     * </ul>
     *
     * <p> {@code page.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not
     * Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
     * Response#status Response.status()}.
     *
     * <p> <strong>NOTE:</strong> {@code page.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
     * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
     *
     * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
     * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
     *
     * <p> Shortcut for main frame's {@link Frame#goto Frame.goto()}
     *
     * @param url URL to navigate page to. The url should include scheme, e.g. {@code https://}.
     */
    fun navigate(url: String): IResponse? = navigate(url, NavigateOptions {})

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect.
     *
     * <p> {@code page.goto} will throw an error if:
     * <ul>
     * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
     * <li> target URL is invalid.</li>
     * <li> the {@code timeout} is exceeded during navigation.</li>
     * <li> the remote server does not respond or is unreachable.</li>
     * <li> the main resource failed to load.</li>
     * </ul>
     *
     * <p> {@code page.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not
     * Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
     * Response#status Response.status()}.
     *
     * <p> <strong>NOTE:</strong> {@code page.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
     * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
     *
     * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
     * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
     *
     * <p> Shortcut for main frame's {@link Frame#goto Frame.goto()}
     *
     * @param url URL to navigate page to. The url should include scheme, e.g. {@code https://}.
     */
    fun navigate(url: String, options: NavigateOptions): IResponse?

}