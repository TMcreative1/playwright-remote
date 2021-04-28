package com.playwright.remote.engine.page.api

import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.dialog.api.IDialog
import com.playwright.remote.engine.download.api.IDownload
import com.playwright.remote.engine.filechooser.api.IFileChooser
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.options.NavigateOptions
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.websocket.api.IWebSocket
import com.playwright.remote.engine.worker.api.IWorker

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
     * Emitted when a file chooser is supposed to appear, such as after clicking the  {@code <input type=file>}. Playwright can
     * respond to it via setting the input files using {@link FileChooser#setFiles FileChooser.setFiles()} that can be uploaded
     * after that.
     * <pre>{@code
     * page.onFileChooser(fileChooser -> {
     *   fileChooser.setFiles(Paths.get("/tmp/myfile.pdf"));
     * });
     * }</pre>
     */
    fun onFileChooser(handler: (IFileChooser) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onFileChooser onFileChooser(handler)}.
     */
    fun offFileChooser(handler: (IFileChooser) -> Unit)

    /**
     * Emitted when a frame is attached.
     */
    fun onFrameAttached(handler: (IFrame) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onFrameAttached onFrameAttached(handler)}.
     */
    fun offFrameAttached(handler: (IFrame) -> Unit)

    /**
     * Emitted when a frame is detached.
     */
    fun onFrameDetached(handler: (IFrame) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onFrameDetached onFrameDetached(handler)}.
     */
    fun offFrameDetached(handler: (IFrame) -> Unit)

    /**
     * Emitted when a frame is navigated to a new url.
     */
    fun onFrameNavigated(handler: (IFrame) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onFrameNavigated onFrameNavigated(handler)}.
     */
    fun offFrameNavigated(handler: (IFrame) -> Unit)

    /**
     * Emitted when the JavaScript <a href="https://developer.mozilla.org/en-US/docs/Web/Events/load">{@code load}</a> event is
     * dispatched.
     */
    fun onLoad(handler: (IPage) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onLoad onLoad(handler)}.
     */
    fun offLoad(handler: (IPage) -> Unit)

    /**
     * Emitted when an uncaught exception happens within the page.
     */
    fun onPageError(handler: (String) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onPageError onPageError(handler)}.
     */
    fun offPageError(handler: (String) -> Unit)

    /**
     * Emitted when the page opens a new tab or window. This event is emitted in addition to the {@link BrowserContext#onPage
     * BrowserContext.onPage()}, but only for popups relevant to this page.
     *
     * <p> The earliest moment that page is available is when it has navigated to the initial url. For example, when opening a
     * popup with {@code window.open('http://example.com')}, this event will fire when the network request to "http://example.com" is
     * done and its response has started loading in the popup.
     * <pre>{@code
     * Page popup = page.waitForPopup(() -> {
     *   page.evaluate("() => window.open('https://example.com')");
     * });
     * System.out.println(popup.evaluate("location.href"));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Use {@link Page#waitForLoadState Page.waitForLoadState()} to wait until the page gets to a particular state (you should
     * not need it in most cases).
     */
    fun onPopup(handler: (IPage) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onPopup onPopup(handler)}.
     */
    fun offPopup(handler: (IPage) -> Unit)

    /**
     * Emitted when a page issues a request. The [request] object is read-only. In order to intercept and mutate requests, see
     * {@link Page#route Page.route()} or {@link BrowserContext#route BrowserContext.route()}.
     */
    fun onRequest(handler: (IRequest) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onRequest onRequest(handler)}.
     */
    fun offRequest(handler: (IRequest) -> Unit)

    /**
     * Emitted when a request fails, for example by timing out.
     *
     * <p> <strong>NOTE:</strong> HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will complete
     * with {@link Page#onRequestFinished Page.onRequestFinished()} event and not with {@link Page#onRequestFailed
     * Page.onRequestFailed()}.
     */
    fun onRequestFailed(handler: (IRequest) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onRequestFailed onRequestFailed(handler)}.
     */
    fun offRequestFailed(handler: (IRequest) -> Unit)

    /**
     * Emitted when a request finishes successfully after downloading the response body. For a successful response, the
     * sequence of events is {@code request}, {@code response} and {@code requestfinished}.
     */
    fun onRequestFinished(handler: (IRequest) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onRequestFinished onRequestFinished(handler)}.
     */
    fun offRequestFinished(handler: (IRequest) -> Unit)

    /**
     * Emitted when [response] status and headers are received for a request. For a successful response, the sequence of events
     * is {@code request}, {@code response} and {@code requestfinished}.
     */
    fun onResponse(handler: (IResponse) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onResponse onResponse(handler)}.
     */
    fun offResponse(handler: (IResponse) -> Unit)

    /**
     * Emitted when {@code WebSocket} request is sent.
     */
    fun onWebSocket(handler: (IWebSocket) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onWebSocket onWebSocket(handler)}.
     */
    fun offWebSocket(handler: (IWebSocket) -> Unit)

    /**
     * Emitted when a dedicated <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">WebWorker</a> is
     * spawned by the page.
     */
    fun onWorker(handler: (IWorker) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onWorker onWorker(handler)}.
     */
    fun offWorker(handler: (IWorker) -> Unit)

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