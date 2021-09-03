package com.playwright.remote.engine.page.api

import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.domain.file.FilePayload
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.callback.api.IBindingCallback
import com.playwright.remote.engine.callback.api.IFunctionCallback
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.dialog.api.IDialog
import com.playwright.remote.engine.download.api.IDownload
import com.playwright.remote.engine.filechooser.api.IFileChooser
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.keyboard.api.IKeyboard
import com.playwright.remote.engine.mouse.api.IMouse
import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.ScreenshotOptions
import com.playwright.remote.engine.options.element.*
import com.playwright.remote.engine.options.element.PressOptions
import com.playwright.remote.engine.options.element.TypeOptions
import com.playwright.remote.engine.options.wait.*
import com.playwright.remote.engine.route.api.IRoute
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.touchscreen.api.ITouchScreen
import com.playwright.remote.engine.video.api.IVideo
import com.playwright.remote.engine.websocket.api.IWebSocket
import com.playwright.remote.engine.worker.api.IWorker
import java.nio.file.Path
import java.util.regex.Pattern

interface IPage : AutoCloseable {
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
     * Adds a script which would be evaluated in one of the following scenarios:
     * <ul>
     * <li> Whenever the page is navigated.</li>
     * <li> Whenever the child frame is attached or navigated. In this case, the script is evaluated in the context of the newly
     * attached frame.</li>
     * </ul>
     *
     * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
     * the JavaScript environment, e.g. to seed {@code Math.random}.
     *
     * <p> An example of overriding {@code Math.random} before the page loads:
     * <pre>{@code
     * // In your playwright script, assuming the preload.js file is in same directory
     * page.addInitScript(Paths.get("./preload.js"));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via {@link BrowserContext#addInitScript
     * BrowserContext.addInitScript()} and {@link Page#addInitScript Page.addInitScript()} is not defined.
     *
     * @param script Script to be evaluated in all pages in the browser context.
     */
    fun addInitScript(script: String)

    /**
     * Adds a script which would be evaluated in one of the following scenarios:
     * <ul>
     * <li> Whenever the page is navigated.</li>
     * <li> Whenever the child frame is attached or navigated. In this case, the script is evaluated in the context of the newly
     * attached frame.</li>
     * </ul>
     *
     * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
     * the JavaScript environment, e.g. to seed {@code Math.random}.
     *
     * <p> An example of overriding {@code Math.random} before the page loads:
     * <pre>{@code
     * // In your playwright script, assuming the preload.js file is in same directory
     * page.addInitScript(Paths.get("./preload.js"));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via {@link BrowserContext#addInitScript
     * BrowserContext.addInitScript()} and {@link Page#addInitScript Page.addInitScript()} is not defined.
     *
     * @param script Script to be evaluated in all pages in the browser context.
     */
    fun addInitScript(script: Path)

    /**
     * Adds a {@code <script>} tag into the page with the desired url or content. Returns the added tag when the script's onload
     * fires or when the script content was injected into frame.
     *
     * <p> Shortcut for main frame's {@link Frame#addScriptTag Frame.addScriptTag()}.
     */
    fun addScriptTag(): IElementHandle {
        return addScriptTag(null)
    }

    /**
     * Adds a {@code <script>} tag into the page with the desired url or content. Returns the added tag when the script's onload
     * fires or when the script content was injected into frame.
     *
     * <p> Shortcut for main frame's {@link Frame#addScriptTag Frame.addScriptTag()}.
     */
    fun addScriptTag(options: AddScriptTagOptions?): IElementHandle

    /**
     * Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the
     * content. Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
     *
     * <p> Shortcut for main frame's {@link Frame#addStyleTag Frame.addStyleTag()}.
     */
    fun addStyleTag(): IElementHandle {
        return addStyleTag(null)
    }

    /**
     * Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the
     * content. Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
     *
     * <p> Shortcut for main frame's {@link Frame#addStyleTag Frame.addStyleTag()}.
     */
    fun addStyleTag(options: AddStyleTagOptions?): IElementHandle

    /**
     * Brings page to front (activates tab).
     */
    fun bringToFront()

    /**
     * This method checks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
     * checked, this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now checked. If not, this method throws.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> Shortcut for main frame's {@link Frame#check Frame.check()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun check(selector: String) {
        check(selector, null)
    }

    /**
     * This method checks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
     * checked, this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now checked. If not, this method throws.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> Shortcut for main frame's {@link Frame#check Frame.check()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun check(selector: String, options: CheckOptions?)

    /**
     * This method clicks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> Shortcut for main frame's {@link Frame#click Frame.click()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun click(selector: String) {
        click(selector, null)
    }

    /**
     * This method clicks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> Shortcut for main frame's {@link Frame#click Frame.click()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun click(selector: String, options: ClickOptions?)

    /**
     * If {@code runBeforeUnload} is {@code false}, does not run any unload handlers and waits for the page to be closed. If
     * {@code runBeforeUnload} is {@code true} the method will run unload handlers, but will **not** wait for the page to close.
     *
     * <p> By default, {@code page.close()} **does not** run {@code beforeunload} handlers.
     *
     * <p> <strong>NOTE:</strong> if {@code runBeforeUnload} is passed as true, a {@code beforeunload} dialog might be summoned and should be handled manually via
     * {@link Page#onDialog Page.onDialog()} event.
     */
    override fun close() {
        close(null)
    }

    /**
     * If {@code runBeforeUnload} is {@code false}, does not run any unload handlers and waits for the page to be closed. If
     * {@code runBeforeUnload} is {@code true} the method will run unload handlers, but will **not** wait for the page to close.
     *
     * <p> By default, {@code page.close()} **does not** run {@code beforeunload} handlers.
     *
     * <p> <strong>NOTE:</strong> if {@code runBeforeUnload} is passed as true, a {@code beforeunload} dialog might be summoned and should be handled manually via
     * {@link Page#onDialog Page.onDialog()} event.
     */
    fun close(options: CloseOptions?)

    /**
     * Gets the full HTML contents of the page, including the doctype.
     */
    fun content(): String

    /**
     * Get the browser context that the page belongs to.
     */
    fun context(): IBrowserContext

    /**
     * This method double clicks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first
     * click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@code page.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
     *
     * <p> Shortcut for main frame's {@link Frame#dblclick Frame.dblclick()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun doubleClick(selector: String) {
        doubleClick(selector, null)
    }

    /**
     * This method double clicks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first
     * click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@code page.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
     *
     * <p> Shortcut for main frame's {@link Frame#dblclick Frame.dblclick()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun doubleClick(selector: String, options: DoubleClickOptions?)

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * page.dispatchEvent("button#submit", "click");
     * }</pre>
     *
     * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
     * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
     *
     * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
     * <ul>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent">DragEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent">FocusEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent">KeyboardEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent">MouseEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent">PointerEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent">TouchEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/Event/Event">Event</a></li>
     * </ul>
     *
     * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
     * <pre>{@code
     * // Note you can only create DataTransfer in Chromium and Firefox
     * JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
     * Map<String, Object> arg = new HashMap<>();
     * arg.put("dataTransfer", dataTransfer);
     * page.dispatchEvent("#source", "dragstart", arg);
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     */
    fun dispatchEvent(selector: String, type: String) {
        dispatchEvent(selector, type, null)
    }

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * page.dispatchEvent("button#submit", "click");
     * }</pre>
     *
     * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
     * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
     *
     * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
     * <ul>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent">DragEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent">FocusEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent">KeyboardEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent">MouseEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent">PointerEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent">TouchEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/Event/Event">Event</a></li>
     * </ul>
     *
     * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
     * <pre>{@code
     * // Note you can only create DataTransfer in Chromium and Firefox
     * JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
     * Map<String, Object> arg = new HashMap<>();
     * arg.put("dataTransfer", dataTransfer);
     * page.dispatchEvent("#source", "dragstart", arg);
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     * @param eventInit Optional event-specific initialization properties.
     */
    fun dispatchEvent(selector: String, type: String, eventInit: Any?) {
        dispatchEvent(selector, type, eventInit, null)
    }

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * page.dispatchEvent("button#submit", "click");
     * }</pre>
     *
     * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
     * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
     *
     * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
     * <ul>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent">DragEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent">FocusEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent">KeyboardEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent">MouseEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent">PointerEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent">TouchEvent</a></li>
     * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/Event/Event">Event</a></li>
     * </ul>
     *
     * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
     * <pre>{@code
     * // Note you can only create DataTransfer in Chromium and Firefox
     * JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
     * Map<String, Object> arg = new HashMap<>();
     * arg.put("dataTransfer", dataTransfer);
     * page.dispatchEvent("#source", "dragstart", arg);
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     * @param eventInit Optional event-specific initialization properties.
     */
    fun dispatchEvent(selector: String, type: String, eventInit: Any?, options: DispatchEventOptions?)

    /**
     * <pre>{@code
     * page.evaluate("() => matchMedia('screen').matches");
     * // → true
     * page.evaluate("() => matchMedia('print').matches");
     * // → false
     *
     * page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
     * page.evaluate("() => matchMedia('screen').matches");
     * // → false
     * page.evaluate("() => matchMedia('print').matches");
     * // → true
     *
     * page.emulateMedia(new Page.EmulateMediaOptions());
     * page.evaluate("() => matchMedia('screen').matches");
     * // → true
     * page.evaluate("() => matchMedia('print').matches");
     * // → false
     * }</pre>
     * <pre>{@code
     * page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));
     * page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches");
     * // → true
     * page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches");
     * // → false
     * page.evaluate("() => matchMedia('(prefers-color-scheme: no-preference)').matches");
     * // → false
     * }</pre>
     */
    fun emulateMedia() {
        emulateMedia(null)
    }

    /**
     * <pre>{@code
     * page.evaluate("() => matchMedia('screen').matches");
     * // → true
     * page.evaluate("() => matchMedia('print').matches");
     * // → false
     *
     * page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
     * page.evaluate("() => matchMedia('screen').matches");
     * // → false
     * page.evaluate("() => matchMedia('print').matches");
     * // → true
     *
     * page.emulateMedia(new Page.EmulateMediaOptions());
     * page.evaluate("() => matchMedia('screen').matches");
     * // → true
     * page.evaluate("() => matchMedia('print').matches");
     * // → false
     * }</pre>
     * <pre>{@code
     * page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));
     * page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches");
     * // → true
     * page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches");
     * // → false
     * page.evaluate("() => matchMedia('(prefers-color-scheme: no-preference)').matches");
     * // → false
     * }</pre>
     */
    fun emulateMedia(options: EmulateMediaOptions?)

    /**
     * The method finds an element matching the specified selector within the page and passes it as a first argument to
     * {@code expression}. If no elements match the selector, the method throws an error. Returns the value of {@code expression}.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Page#evalOnSelector Page.evalOnSelector()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * String searchValue = (String) page.evalOnSelector("#search", "el => el.value");
     * String preloadHref = (String) page.evalOnSelector("link[rel=preload]", "el => el.href");
     * String html = (String) page.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#evalOnSelector Frame.evalOnSelector()}.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evalOnSelector(selector: String, expression: String): Any {
        return evalOnSelector(selector, expression, null)
    }

    /**
     * The method finds an element matching the specified selector within the page and passes it as a first argument to
     * {@code expression}. If no elements match the selector, the method throws an error. Returns the value of {@code expression}.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Page#evalOnSelector Page.evalOnSelector()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * String searchValue = (String) page.evalOnSelector("#search", "el => el.value");
     * String preloadHref = (String) page.evalOnSelector("link[rel=preload]", "el => el.href");
     * String html = (String) page.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#evalOnSelector Frame.evalOnSelector()}.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evalOnSelector(selector: String, expression: String, arg: Any?): Any

    /**
     * The method finds all elements matching the specified selector within the page and passes an array of matched elements as
     * a first argument to {@code expression}. Returns the result of {@code expression} invocation.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Page#evalOnSelectorAll Page.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * boolean divCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evalOnSelectorAll(selector: String, expression: String): Any {
        return evalOnSelectorAll(selector, expression, null)
    }

    /**
     * The method finds all elements matching the specified selector within the page and passes an array of matched elements as
     * a first argument to {@code expression}. Returns the result of {@code expression} invocation.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Page#evalOnSelectorAll Page.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * boolean divCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evalOnSelectorAll(selector: String, expression: String, arg: Any?): Any

    /**
     * Returns the value of the {@code expression} invocation.
     *
     * <p> If the function passed to the {@link Page#evaluate Page.evaluate()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Page#evaluate Page.evaluate()} would wait for the promise to resolve and return its value.
     *
     * <p> If the function passed to the {@link Page#evaluate Page.evaluate()} returns a non-[Serializable] value, then {@link
     * Page#evaluate Page.evaluate()} resolves to {@code undefined}. Playwright also supports transferring some additional values
     * that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
     *
     * <p> Passing argument to {@code expression}:
     * <pre>{@code
     * Object result = page.evaluate("([x, y]) => {\n" +
     *   "  return Promise.resolve(x * y);\n" +
     *   "}", Arrays.asList(7, 8));
     * System.out.println(result); // prints "56"
     * }</pre>
     *
     * <p> A string can also be passed in instead of a function:
     * <pre>{@code
     * System.out.println(page.evaluate("1 + 2")); // prints "3"
     * }</pre>
     *
     * <p> {@code ElementHandle} instances can be passed as an argument to the {@link Page#evaluate Page.evaluate()}:
     * <pre>{@code
     * ElementHandle bodyHandle = page.querySelector("body");
     * String html = (String) page.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
     * bodyHandle.dispose();
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#evaluate Frame.evaluate()}.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluate(expression: String): Any {
        return evaluate(expression, null)
    }

    /**
     * Returns the value of the {@code expression} invocation.
     *
     * <p> If the function passed to the {@link Page#evaluate Page.evaluate()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Page#evaluate Page.evaluate()} would wait for the promise to resolve and return its value.
     *
     * <p> If the function passed to the {@link Page#evaluate Page.evaluate()} returns a non-[Serializable] value, then {@link
     * Page#evaluate Page.evaluate()} resolves to {@code undefined}. Playwright also supports transferring some additional values
     * that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
     *
     * <p> Passing argument to {@code expression}:
     * <pre>{@code
     * Object result = page.evaluate("([x, y]) => {\n" +
     *   "  return Promise.resolve(x * y);\n" +
     *   "}", Arrays.asList(7, 8));
     * System.out.println(result); // prints "56"
     * }</pre>
     *
     * <p> A string can also be passed in instead of a function:
     * <pre>{@code
     * System.out.println(page.evaluate("1 + 2")); // prints "3"
     * }</pre>
     *
     * <p> {@code ElementHandle} instances can be passed as an argument to the {@link Page#evaluate Page.evaluate()}:
     * <pre>{@code
     * ElementHandle bodyHandle = page.querySelector("body");
     * String html = (String) page.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
     * bodyHandle.dispose();
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#evaluate Frame.evaluate()}.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluate(expression: String, arg: Any?): Any

    /**
     * Returns the value of the {@code expression} invocation as a {@code JSHandle}.
     *
     * <p> The only difference between {@link Page#evaluate Page.evaluate()} and {@link Page#evaluateHandle Page.evaluateHandle()}
     * is that {@link Page#evaluateHandle Page.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function passed to the {@link Page#evaluateHandle Page.evaluateHandle()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Page#evaluateHandle Page.evaluateHandle()} would wait for the promise to resolve and return its value.
     * <pre>{@code
     * // Handle for the window object.
     * JSHandle aWindowHandle = page.evaluateHandle("() => Promise.resolve(window)");
     * }</pre>
     *
     * <p> A string can also be passed in instead of a function:
     * <pre>{@code
     * JSHandle aHandle = page.evaluateHandle("document"); // Handle for the "document".
     * }</pre>
     *
     * <p> {@code JSHandle} instances can be passed as an argument to the {@link Page#evaluateHandle Page.evaluateHandle()}:
     * <pre>{@code
     * JSHandle aHandle = page.evaluateHandle("() => document.body");
     * JSHandle resultHandle = page.evaluateHandle("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(aHandle, "hello"));
     * System.out.println(resultHandle.jsonValue());
     * resultHandle.dispose();
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluateHandle(expression: String): IJSHandle {
        return evaluateHandle(expression, null)
    }

    /**
     * Returns the value of the {@code expression} invocation as a {@code JSHandle}.
     *
     * <p> The only difference between {@link Page#evaluate Page.evaluate()} and {@link Page#evaluateHandle Page.evaluateHandle()}
     * is that {@link Page#evaluateHandle Page.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function passed to the {@link Page#evaluateHandle Page.evaluateHandle()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Page#evaluateHandle Page.evaluateHandle()} would wait for the promise to resolve and return its value.
     * <pre>{@code
     * // Handle for the window object.
     * JSHandle aWindowHandle = page.evaluateHandle("() => Promise.resolve(window)");
     * }</pre>
     *
     * <p> A string can also be passed in instead of a function:
     * <pre>{@code
     * JSHandle aHandle = page.evaluateHandle("document"); // Handle for the "document".
     * }</pre>
     *
     * <p> {@code JSHandle} instances can be passed as an argument to the {@link Page#evaluateHandle Page.evaluateHandle()}:
     * <pre>{@code
     * JSHandle aHandle = page.evaluateHandle("() => document.body");
     * JSHandle resultHandle = page.evaluateHandle("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(aHandle, "hello"));
     * System.out.println(resultHandle.jsonValue());
     * resultHandle.dispose();
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluateHandle(expression: String, arg: Any?): IJSHandle

    /**
     * The method adds a function called {@code name} on the {@code window} object of every frame in this page. When called, the function
     * executes {@code callback} and returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
     * resolves to the return value of {@code callback}. If the {@code callback} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
     * awaited.
     *
     * <p> The first argument of the {@code callback} function contains information about the caller: {@code { browserContext: BrowserContext,
     * page: Page, frame: Frame }}.
     *
     * <p> See {@link BrowserContext#exposeBinding BrowserContext.exposeBinding()} for the context-wide version.
     *
     * <p> <strong>NOTE:</strong> Functions installed via {@link Page#exposeBinding Page.exposeBinding()} survive navigations.
     *
     * <p> An example of exposing page URL to all frames in a page:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType webkit = playwright.webkit();
     *       Browser browser = webkit.launch({ headless: false });
     *       BrowserContext context = browser.newContext();
     *       Page page = context.newPage();
     *       page.exposeBinding("pageURL", (source, args) -> source.page().url());
     *       page.setContent("<script>\n" +
     *         "  async function onClick() {\n" +
     *         "    document.querySelector('div').textContent = await window.pageURL();\n" +
     *         "  }\n" +
     *         "</script>\n" +
     *         "<button onclick=\"onClick()\">Click me</button>\n" +
     *         "<div></div>");
     *       page.click("button");
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> An example of passing an element handle:
     * <pre>{@code
     * page.exposeBinding("clicked", (source, args) -> {
     *   ElementHandle element = (ElementHandle) args[0];
     *   System.out.println(element.textContent());
     *   return null;
     * }, new Page.ExposeBindingOptions().setHandle(true));
     * page.setContent("" +
     *   "<script>\n" +
     *   "  document.addEventListener('click', event => window.clicked(event.target));\n" +
     *   "</script>\n" +
     *   "<div>Click me</div>\n" +
     *   "<div>Or click me</div>\n");
     * }</pre>
     *
     * @param name Name of the function on the window object.
     * @param callback Callback function that will be called in the Playwright's context.
     */
    fun exposeBinding(name: String, callback: IBindingCallback) {
        exposeBinding(name, callback, null)
    }

    /**
     * The method adds a function called {@code name} on the {@code window} object of every frame in this page. When called, the function
     * executes {@code callback} and returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
     * resolves to the return value of {@code callback}. If the {@code callback} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
     * awaited.
     *
     * <p> The first argument of the {@code callback} function contains information about the caller: {@code { browserContext: BrowserContext,
     * page: Page, frame: Frame }}.
     *
     * <p> See {@link BrowserContext#exposeBinding BrowserContext.exposeBinding()} for the context-wide version.
     *
     * <p> <strong>NOTE:</strong> Functions installed via {@link Page#exposeBinding Page.exposeBinding()} survive navigations.
     *
     * <p> An example of exposing page URL to all frames in a page:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType webkit = playwright.webkit();
     *       Browser browser = webkit.launch({ headless: false });
     *       BrowserContext context = browser.newContext();
     *       Page page = context.newPage();
     *       page.exposeBinding("pageURL", (source, args) -> source.page().url());
     *       page.setContent("<script>\n" +
     *         "  async function onClick() {\n" +
     *         "    document.querySelector('div').textContent = await window.pageURL();\n" +
     *         "  }\n" +
     *         "</script>\n" +
     *         "<button onclick=\"onClick()\">Click me</button>\n" +
     *         "<div></div>");
     *       page.click("button");
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> An example of passing an element handle:
     * <pre>{@code
     * page.exposeBinding("clicked", (source, args) -> {
     *   ElementHandle element = (ElementHandle) args[0];
     *   System.out.println(element.textContent());
     *   return null;
     * }, new Page.ExposeBindingOptions().setHandle(true));
     * page.setContent("" +
     *   "<script>\n" +
     *   "  document.addEventListener('click', event => window.clicked(event.target));\n" +
     *   "</script>\n" +
     *   "<div>Click me</div>\n" +
     *   "<div>Or click me</div>\n");
     * }</pre>
     *
     * @param name Name of the function on the window object.
     * @param callback Callback function that will be called in the Playwright's context.
     */
    fun exposeBinding(name: String, callback: IBindingCallback, options: ExposeBindingOptions?)

    /**
     * The method adds a function called {@code name} on the {@code window} object of every frame in the page. When called, the function
     * executes {@code callback} and returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
     * resolves to the return value of {@code callback}.
     *
     * <p> If the {@code callback} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
     * awaited.
     *
     * <p> See {@link BrowserContext#exposeFunction BrowserContext.exposeFunction()} for context-wide exposed function.
     *
     * <p> <strong>NOTE:</strong> Functions installed via {@link Page#exposeFunction Page.exposeFunction()} survive navigations.
     *
     * <p> An example of adding an {@code sha1} function to the page:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * import java.nio.charset.StandardCharsets;
     * import java.security.MessageDigest;
     * import java.security.NoSuchAlgorithmException;
     * import java.util.Base64;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType webkit = playwright.webkit();
     *       Browser browser = webkit.launch({ headless: false });
     *       Page page = browser.newPage();
     *       page.exposeFunction("sha1", args -> {
     *         String text = (String) args[0];
     *         MessageDigest crypto;
     *         try {
     *           crypto = MessageDigest.getInstance("SHA-1");
     *         } catch (NoSuchAlgorithmException e) {
     *           return null;
     *         }
     *         byte[] token = crypto.digest(text.getBytes(StandardCharsets.UTF_8));
     *         return Base64.getEncoder().encodeToString(token);
     *       });
     *       page.setContent("<script>\n" +
     *         "  async function onClick() {\n" +
     *         "    document.querySelector('div').textContent = await window.sha1('PLAYWRIGHT');\n" +
     *         "  }\n" +
     *         "</script>\n" +
     *         "<button onclick=\"onClick()\">Click me</button>\n" +
     *         "<div></div>\n");
     *       page.click("button");
     *     }
     *   }
     * }
     * }</pre>
     *
     * @param name Name of the function on the window object
     * @param callback Callback function which will be called in Playwright's context.
     */
    fun exposeFunction(name: String, callback: IFunctionCallback)

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the element, fills it and
     * triggers an {@code input} event after filling. If the element is inside the {@code <label>} element that has associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, that control will be
     * filled instead. If the element to be filled is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this
     * method throws an error. Note that you can pass an empty string to clear the input field.
     *
     * <p> To send fine-grained keyboard events, use {@link Page#type Page.type()}.
     *
     * <p> Shortcut for main frame's {@link Frame#fill Frame.fill()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
     */
    fun fill(selector: String, value: String) {
        fill(selector, value, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the element, fills it and
     * triggers an {@code input} event after filling. If the element is inside the {@code <label>} element that has associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, that control will be
     * filled instead. If the element to be filled is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this
     * method throws an error. Note that you can pass an empty string to clear the input field.
     *
     * <p> To send fine-grained keyboard events, use {@link Page#type Page.type()}.
     *
     * <p> Shortcut for main frame's {@link Frame#fill Frame.fill()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
     */
    fun fill(selector: String, value: String, options: FillOptions?)

    /**
     * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method
     * waits until a matching element appears in the DOM.
     *
     * <p> Shortcut for main frame's {@link Frame#focus Frame.focus()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun focus(selector: String) {
        focus(selector, null)
    }

    /**
     * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method
     * waits until a matching element appears in the DOM.
     *
     * <p> Shortcut for main frame's {@link Frame#focus Frame.focus()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun focus(selector: String, options: FocusOptions?)

    /**
     * Returns frame matching the specified criteria. Either {@code name} or {@code url} must be specified.
     * <pre>{@code
     * Frame frame = page.frame("frame-name");
     * }</pre>
     * <pre>{@code
     * Frame frame = page.frameByUrl(Pattern.compile(".*domain.*");
     * }</pre>
     *
     * @param name Frame name specified in the {@code iframe}'s {@code name} attribute.
     */
    fun frame(name: String): IFrame?

    /**
     * Returns frame with matching URL.
     *
     * @param url A glob pattern, regex pattern or predicate receiving frame's {@code url} as a [URL] object.
     */
    fun frameByUrl(url: String): IFrame?

    /**
     * Returns frame with matching URL.
     *
     * @param url A glob pattern, regex pattern or predicate receiving frame's {@code url} as a [URL] object.
     */
    fun frameByUrl(url: Pattern): IFrame?

    /**
     * Returns frame with matching URL.
     *
     * @param url A glob pattern, regex pattern or predicate receiving frame's {@code url} as a [URL] object.
     */
    fun frameByUrl(url: (String) -> Boolean): IFrame?

    /**
     * An array of all frames attached to the page.
     */
    fun frames(): List<IFrame>

    /**
     * Returns element attribute value.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param name Attribute name to get the value for.
     */
    fun getAttribute(selector: String, name: String): String? {
        return getAttribute(selector, name, null)
    }

    /**
     * Returns element attribute value.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param name Attribute name to get the value for.
     */
    fun getAttribute(selector: String, name: String, options: GetAttributeOptions?): String?

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect. If can not go back, returns {@code null}.
     *
     * <p> Navigate to the previous page in history.
     */
    fun goBack(): IResponse? {
        return goBack(null)
    }

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect. If can not go back, returns {@code null}.
     *
     * <p> Navigate to the previous page in history.
     */
    fun goBack(options: GoBackOptions?): IResponse?

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect. If can not go forward, returns {@code null}.
     *
     * <p> Navigate to the next page in history.
     */
    fun goForward(): IResponse? {
        return goForward(null)
    }

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect. If can not go forward, returns {@code null}.
     *
     * <p> Navigate to the next page in history.
     */
    fun goForward(options: GoForwardOptions?): IResponse?

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect.
     *
     * <P> {@CODE PAGE.GOTO} WILL THROW AN ERROR IF:
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

    /**
     * This method hovers over an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> Shortcut for main frame's {@link Frame#hover Frame.hover()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun hover(selector: String) {
        hover(selector, null)
    }

    /**
     * This method hovers over an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> Shortcut for main frame's {@link Frame#hover Frame.hover()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun hover(selector: String, options: HoverOptions?)

    /**
     * Returns {@code element.innerHTML}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun innerHTML(selector: String): String {
        return innerHTML(selector, null)
    }

    /**
     * Returns {@code element.innerHTML}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun innerHTML(selector: String, options: InnerHTMLOptions?): String

    /**
     * Returns {@code element.innerText}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun innerText(selector: String): String {
        return innerText(selector, null)
    }

    /**
     * Returns {@code element.innerText}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun innerText(selector: String, options: InnerTextOptions?): String

    /**
     * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} element. Throws for non-input elements.
     *
     * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
     * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun inputValue(selector: String): String {
        return inputValue(selector, null)
    }

    /**
     * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} element. Throws for non-input elements.
     *
     * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
     * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun inputValue(selector: String, options: InputValueOptions?): String


    /**
     * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isChecked(selector: String): Boolean {
        return isChecked(selector, null)
    }

    /**
     * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isChecked(selector: String, options: IsCheckedOptions?): Boolean

    /**
     * Indicates that the page has been closed.
     */
    fun isClosed(): Boolean

    /**
     * Returns whether the element is disabled, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isDisabled(selector: String): Boolean {
        return isDisabled(selector, null)
    }

    /**
     * Returns whether the element is disabled, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isDisabled(selector: String, options: IsDisabledOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isEditable(selector: String): Boolean {
        return isEditable(selector, null)
    }

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isEditable(selector: String, options: IsEditableOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isEnabled(selector: String): Boolean {
        return isEnabled(selector, null)
    }

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isEnabled(selector: String, options: IsEnabledOptions?): Boolean

    /**
     * Returns whether the element is hidden, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.  {@code selector} that does not match any elements
     * is considered hidden.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isHidden(selector: String): Boolean {
        return isHidden(selector, null)
    }

    /**
     * Returns whether the element is hidden, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.  {@code selector} that does not match any elements
     * is considered hidden.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isHidden(selector: String, options: IsHiddenOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>. {@code selector}
     * that does not match any elements is considered not visible.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isVisible(selector: String): Boolean {
        return isVisible(selector, null)
    }

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>. {@code selector}
     * that does not match any elements is considered not visible.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun isVisible(selector: String, options: IsVisibleOptions?): Boolean

    fun keyboard(): IKeyboard

    /**
     * The page's main frame. Page is guaranteed to have a main frame which persists during navigations.
     */
    fun mainFrame(): IFrame

    fun mouse(): IMouse

    /**
     * Returns the opener for popup pages and {@code null} for others. If the opener has been closed already the returns {@code null}.
     */
    fun opener(): IPage?

    /**
     * Pauses script execution. Playwright will stop executing the script and wait for the user to either press 'Resume' button
     * in the page overlay or to call {@code playwright.resume()} in the DevTools console.
     *
     * <p> User can inspect selectors or perform manual steps while paused. Resume will continue running the original script from
     * the place it was paused.
     *
     * <p> <strong>NOTE:</strong> This method requires Playwright to be started in a headed mode, with a falsy {@code headless} value in the {@link
     * BrowserType#launch BrowserType.launch()}.
     */
    fun pause()

    /**
     * Returns the PDF buffer.
     *
     * <p> <strong>NOTE:</strong> Generating a pdf is currently only supported in Chromium headless.
     *
     * <p> {@code page.pdf()} generates a pdf of the page with {@code print} css media. To generate a pdf with {@code screen} media, call {@link
     * Page#emulateMedia Page.emulateMedia()} before calling {@code page.pdf()}:
     *
     * <p> <strong>NOTE:</strong> By default, {@code page.pdf()} generates a pdf with modified colors for printing. Use the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/CSS/-webkit-print-color-adjust">{@code -webkit-print-color-adjust}</a>
     * property to force rendering of exact colors.
     * <pre>{@code
     * // Generates a PDF with "screen" media type.
     * page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
     * page.pdf(new Page.PdfOptions().setPath(Paths.get("page.pdf")));
     * }</pre>
     *
     * <p> The {@code width}, {@code height}, and {@code margin} options accept values labeled with units. Unlabeled values are treated as pixels.
     *
     * <p> A few examples:
     * <ul>
     * <li> {@code page.pdf({width: 100})} - prints with width set to 100 pixels</li>
     * <li> {@code page.pdf({width: '100px'})} - prints with width set to 100 pixels</li>
     * <li> {@code page.pdf({width: '10cm'})} - prints with width set to 10 centimeters.</li>
     * </ul>
     *
     * <p> All possible units are:
     * <ul>
     * <li> {@code px} - pixel</li>
     * <li> {@code in} - inch</li>
     * <li> {@code cm} - centimeter</li>
     * <li> {@code mm} - millimeter</li>
     * </ul>
     *
     * <p> The {@code format} options are:
     * <ul>
     * <li> {@code Letter}: 8.5in x 11in</li>
     * <li> {@code Legal}: 8.5in x 14in</li>
     * <li> {@code Tabloid}: 11in x 17in</li>
     * <li> {@code Ledger}: 17in x 11in</li>
     * <li> {@code A0}: 33.1in x 46.8in</li>
     * <li> {@code A1}: 23.4in x 33.1in</li>
     * <li> {@code A2}: 16.54in x 23.4in</li>
     * <li> {@code A3}: 11.7in x 16.54in</li>
     * <li> {@code A4}: 8.27in x 11.7in</li>
     * <li> {@code A5}: 5.83in x 8.27in</li>
     * <li> {@code A6}: 4.13in x 5.83in</li>
     * </ul>
     *
     * <p> <strong>NOTE:</strong> {@code headerTemplate} and {@code footerTemplate} markup have the following limitations: > 1. Script tags inside templates are not
     * evaluated. > 2. Page styles are not visible inside templates.
     */
    fun pdf(): ByteArray {
        return pdf(null)
    }

    /**
     * Returns the PDF buffer.
     *
     * <p> <strong>NOTE:</strong> Generating a pdf is currently only supported in Chromium headless.
     *
     * <p> {@code page.pdf()} generates a pdf of the page with {@code print} css media. To generate a pdf with {@code screen} media, call {@link
     * Page#emulateMedia Page.emulateMedia()} before calling {@code page.pdf()}:
     *
     * <p> <strong>NOTE:</strong> By default, {@code page.pdf()} generates a pdf with modified colors for printing. Use the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/CSS/-webkit-print-color-adjust">{@code -webkit-print-color-adjust}</a>
     * property to force rendering of exact colors.
     * <pre>{@code
     * // Generates a PDF with "screen" media type.
     * page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
     * page.pdf(new Page.PdfOptions().setPath(Paths.get("page.pdf")));
     * }</pre>
     *
     * <p> The {@code width}, {@code height}, and {@code margin} options accept values labeled with units. Unlabeled values are treated as pixels.
     *
     * <p> A few examples:
     * <ul>
     * <li> {@code page.pdf({width: 100})} - prints with width set to 100 pixels</li>
     * <li> {@code page.pdf({width: '100px'})} - prints with width set to 100 pixels</li>
     * <li> {@code page.pdf({width: '10cm'})} - prints with width set to 10 centimeters.</li>
     * </ul>
     *
     * <p> All possible units are:
     * <ul>
     * <li> {@code px} - pixel</li>
     * <li> {@code in} - inch</li>
     * <li> {@code cm} - centimeter</li>
     * <li> {@code mm} - millimeter</li>
     * </ul>
     *
     * <p> The {@code format} options are:
     * <ul>
     * <li> {@code Letter}: 8.5in x 11in</li>
     * <li> {@code Legal}: 8.5in x 14in</li>
     * <li> {@code Tabloid}: 11in x 17in</li>
     * <li> {@code Ledger}: 17in x 11in</li>
     * <li> {@code A0}: 33.1in x 46.8in</li>
     * <li> {@code A1}: 23.4in x 33.1in</li>
     * <li> {@code A2}: 16.54in x 23.4in</li>
     * <li> {@code A3}: 11.7in x 16.54in</li>
     * <li> {@code A4}: 8.27in x 11.7in</li>
     * <li> {@code A5}: 5.83in x 8.27in</li>
     * <li> {@code A6}: 4.13in x 5.83in</li>
     * </ul>
     *
     * <p> <strong>NOTE:</strong> {@code headerTemplate} and {@code footerTemplate} markup have the following limitations: > 1. Script tags inside templates are not
     * evaluated. > 2. Page styles are not visible inside templates.
     */
    fun pdf(options: PdfOptions?): ByteArray

    /**
     * Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
     *
     * <p> {@code key} can specify the intended <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
     * character to generate the text for. A superset of the {@code key} values can be found <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
     *
     * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab},
     * {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
     *
     * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
     *
     * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
     *
     * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective
     * texts.
     *
     * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with the
     * modifier, modifier is pressed and being held while the subsequent key is being pressed.
     * <pre>{@code
     * Page page = browser.newPage();
     * page.navigate("https://keycode.info");
     * page.press("body", "A");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("A.png")));
     * page.press("body", "ArrowLeft");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ArrowLeft.png" )));
     * page.press("body", "Shift+O");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("O.png" )));
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(selector: String, key: String) {
        press(selector, key, null)
    }

    /**
     * Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
     *
     * <p> {@code key} can specify the intended <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
     * character to generate the text for. A superset of the {@code key} values can be found <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
     *
     * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab},
     * {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
     *
     * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
     *
     * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
     *
     * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective
     * texts.
     *
     * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with the
     * modifier, modifier is pressed and being held while the subsequent key is being pressed.
     * <pre>{@code
     * Page page = browser.newPage();
     * page.navigate("https://keycode.info");
     * page.press("body", "A");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("A.png")));
     * page.press("body", "ArrowLeft");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ArrowLeft.png" )));
     * page.press("body", "Shift+O");
     * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("O.png" )));
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(selector: String, key: String, options: PressOptions?)

    /**
     * The method finds an element matching the specified selector within the page. If no elements match the selector, the
     * return value resolves to {@code null}.
     *
     * <p> Shortcut for main frame's {@link Frame#querySelector Frame.querySelector()}.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelector(selector: String?): IElementHandle?

    /**
     * The method finds all elements matching the specified selector within the page. If no elements match the selector, the
     * return value resolves to {@code []}.
     *
     * <p> Shortcut for main frame's {@link Frame#querySelectorAll Frame.querySelectorAll()}.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelectorAll(selector: String?): List<IElementHandle>?

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect.
     */
    fun reload(): IResponse? {
        return reload(null)
    }

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect.
     */
    fun reload(options: ReloadOptions?): IResponse?

    /**
     * Routing provides the capability to modify network requests that are made by a page.
     *
     * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
     *
     * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
     *
     * <p> An example of a naive handler that aborts all image requests:
     * <pre>{@code
     * Page page = browser.newPage();
     * page.route("**\/ *.{png,jpg,jpeg}", route -> route.abort());
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> or the same snippet using a regex pattern instead:
     * <pre>{@code
     * Page page = browser.newPage();
     * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
     * BrowserContext.route()}) when request matches both handlers.
     *
     * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
     *
     * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler handler function to route the request.
     */
    fun route(url: String, handler: (IRoute) -> Unit)

    /**
     * Routing provides the capability to modify network requests that are made by a page.
     *
     * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
     *
     * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
     *
     * <p> An example of a naive handler that aborts all image requests:
     * <pre>{@code
     * Page page = browser.newPage();
     * page.route("**\/ *.{png,jpg,jpeg}", route -> route.abort());
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> or the same snippet using a regex pattern instead:
     * <pre>{@code
     * Page page = browser.newPage();
     * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
     * BrowserContext.route()}) when request matches both handlers.
     *
     * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
     *
     * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler handler function to route the request.
     */
    fun route(url: Pattern, handler: (IRoute) -> Unit)

    /**
     * Routing provides the capability to modify network requests that are made by a page.
     *
     * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
     *
     * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
     *
     * <p> An example of a naive handler that aborts all image requests:
     * <pre>{@code
     * Page page = browser.newPage();
     * page.route("**\/ *.{png,jpg,jpeg}", route -> route.abort());
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> or the same snippet using a regex pattern instead:
     * <pre>{@code
     * Page page = browser.newPage();
     * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
     * page.navigate("https://example.com");
     * browser.close();
     * }</pre>
     *
     * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
     * BrowserContext.route()}) when request matches both handlers.
     *
     * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
     *
     * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler handler function to route the request.
     */
    fun route(url: (String) -> Boolean, handler: (IRoute) -> Unit)

    /**
     * Returns the buffer with the captured screenshot.
     */
    fun screenshot(): ByteArray {
        return screenshot(null)
    }

    /**
     * Returns the buffer with the captured screenshot.
     */
    fun screenshot(options: ScreenshotOptions?): ByteArray

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: String): List<String> {
        return selectOption(selector, value, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: String?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: IElementHandle): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: IElementHandle?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<String>?): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<String>?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: SelectOption): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: SelectOption?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<IElementHandle>?): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<IElementHandle>?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<SelectOption>?): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
     * matching {@code selector}, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * page.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#selectOption Frame.selectOption()}
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<SelectOption>?, options: SelectOptionOptions?): List<String>

    /**
     *
     *
     * @param html HTML markup to assign to the page.
     */
    fun setContent(html: String) {
        setContent(html, null)
    }

    /**
     *
     *
     * @param html HTML markup to assign to the page.
     */
    fun setContent(html: String, options: SetContentOptions?)

    /**
     * This setting will change the default maximum navigation time for the following methods and related shortcuts:
     * <ul>
     * <li> {@link Page#goBack Page.goBack()}</li>
     * <li> {@link Page#goForward Page.goForward()}</li>
     * <li> {@link Page#goto Page.goto()}</li>
     * <li> {@link Page#reload Page.reload()}</li>
     * <li> {@link Page#setContent Page.setContent()}</li>
     * <li> {@link Page#waitForNavigation Page.waitForNavigation()}</li>
     * <li> {@link Page#waitForURL Page.waitForURL()}</li>
     * </ul>
     *
     * <p> <strong>NOTE:</strong> {@link Page#setDefaultNavigationTimeout Page.setDefaultNavigationTimeout()} takes priority over {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()}, {@link BrowserContext#setDefaultTimeout
     * BrowserContext.setDefaultTimeout()} and {@link BrowserContext#setDefaultNavigationTimeout
     * BrowserContext.setDefaultNavigationTimeout()}.
     *
     * @param timeout Maximum navigation time in milliseconds
     */
    fun setDefaultNavigationTimeout(timeout: Double)

    /**
     * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
     *
     * <p> <strong>NOTE:</strong> {@link Page#setDefaultNavigationTimeout Page.setDefaultNavigationTimeout()} takes priority over {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()}.
     *
     * @param timeout Maximum time in milliseconds
     */
    fun setDefaultTimeout(timeout: Double)

    /**
     * The extra HTTP headers will be sent with every request the page initiates.
     *
     * <p> <strong>NOTE:</strong> {@link Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()} does not guarantee the order of headers in the outgoing
     * requests.
     *
     * @param headers An object containing additional HTTP headers to be sent with every request. All header values must be strings.
     */
    fun setExtraHTTPHeaders(headers: Map<String, String>)

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Path) {
        setInputFiles(selector, files, null)
    }

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Path, options: SetInputFilesOptions?)

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Array<Path>) {
        setInputFiles(selector, files, null)
    }

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Array<Path>, options: SetInputFilesOptions?)

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: FilePayload) {
        setInputFiles(selector, files, null)
    }

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: FilePayload, options: SetInputFilesOptions?)

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Array<FilePayload>) {
        setInputFiles(selector, files, null)
    }

    /**
     * This method expects {@code selector} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun setInputFiles(selector: String, files: Array<FilePayload>, options: SetInputFilesOptions?)

    /**
     * In the case of multiple pages in a single browser, each page can have its own viewport size. However, {@link
     * Browser#newContext Browser.newContext()} allows to set viewport size (and more) for all pages in the context at once.
     *
     * <p> {@code page.setViewportSize} will resize the page. A lot of websites don't expect phones to change size, so you should set the
     * viewport size before navigating to the page.
     * <pre>{@code
     * Page page = browser.newPage();
     * page.setViewportSize(640, 480);
     * page.navigate("https://example.com");
     * }</pre>
     */
    fun setViewportSize(width: Int, height: Int)

    /**
     * This method taps an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@link Page#tap Page.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
     *
     * <p> Shortcut for main frame's {@link Frame#tap Frame.tap()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun tap(selector: String) {
        tap(selector, null)
    }

    /**
     * This method taps an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@link Page#tap Page.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
     *
     * <p> Shortcut for main frame's {@link Frame#tap Frame.tap()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun tap(selector: String, options: TapOptions?)

    /**
     * Returns {@code element.textContent}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun textContent(selector: String): String {
        return textContent(selector, null)
    }

    /**
     * Returns {@code element.textContent}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun textContent(selector: String, options: TextContentOptions?): String

    /**
     * Returns the page's title. Shortcut for main frame's {@link Frame#title Frame.title()}.
     */
    fun title(): String

    fun touchScreen(): ITouchScreen

    /**
     * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code page.type} can be used to send
     * fine-grained keyboard events. To fill values in form fields, use {@link Page#fill Page.fill()}.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
     * <pre>{@code
     * // Types instantly
     * page.type("#mytextarea", "Hello");
     * // Types slower, like a user
     * page.type("#mytextarea", "World", new Page.TypeOptions().setDelay(100));
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#type Frame.type()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param text A text to type into a focused element.
     */
    fun type(selector: String, text: String) {
        type(selector, text, null)
    }

    /**
     * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code page.type} can be used to send
     * fine-grained keyboard events. To fill values in form fields, use {@link Page#fill Page.fill()}.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
     * <pre>{@code
     * // Types instantly
     * page.type("#mytextarea", "Hello");
     * // Types slower, like a user
     * page.type("#mytextarea", "World", new Page.TypeOptions().setDelay(100));
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#type Frame.type()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param text A text to type into a focused element.
     */
    fun type(selector: String, text: String, options: TypeOptions?)

    /**
     * This method unchecks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
     * unchecked, this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> Shortcut for main frame's {@link Frame#uncheck Frame.uncheck()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun uncheck(selector: String) {
        uncheck(selector, null)
    }

    /**
     * This method unchecks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
     * unchecked, this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> Shortcut for main frame's {@link Frame#uncheck Frame.uncheck()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun uncheck(selector: String, options: UncheckOptions?)

    /**
     * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes for
     * the {@code url}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     */
    fun unroute(url: String) {
        unroute(url, null)
    }

    /**
     * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes for
     * the {@code url}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler Optional handler function to route the request.
     */
    fun unroute(url: String, handler: ((IRoute) -> Unit)?)

    /**
     * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes for
     * the {@code url}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     */
    fun unroute(url: Pattern) {
        unroute(url, null)
    }

    /**
     * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes for
     * the {@code url}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler Optional handler function to route the request.
     */
    fun unroute(url: Pattern, handler: ((IRoute) -> Unit)?)

    /**
     * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes for
     * the {@code url}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     */
    fun unroute(url: (String) -> Boolean) {
        unroute(url, null)
    }

    /**
     * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes for
     * the {@code url}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
     * @param handler Optional handler function to route the request.
     */
    fun unroute(url: (String) -> Boolean, handler: ((IRoute) -> Unit)?)

    /**
     * Shortcut for main frame's {@link Frame#url Frame.url()}.
     */
    fun url(): String

    /**
     * Video object associated with this page.
     */
    fun video(): IVideo?

    fun viewportSize(): ViewportSize

    /**
     * Performs action and waits for the Page to close.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForClose(callback: () -> Unit): IPage? {
        return waitForClose(null, callback)
    }

    /**
     * Performs action and waits for the Page to close.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForClose(options: WaitForCloseOptions?, callback: () -> Unit): IPage?

    /**
     * Performs action and waits for a {@code ConsoleMessage} to be logged by in the page. If predicate is provided, it passes
     * {@code ConsoleMessage} value into the {@code predicate} function and waits for {@code predicate(message)} to return a truthy value. Will
     * throw an error if the page is closed before the console event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForConsoleMessage(callback: () -> Unit): IConsoleMessage? {
        return waitForConsoleMessage(null, callback)
    }

    /**
     * Performs action and waits for a {@code ConsoleMessage} to be logged by in the page. If predicate is provided, it passes
     * {@code ConsoleMessage} value into the {@code predicate} function and waits for {@code predicate(message)} to return a truthy value. Will
     * throw an error if the page is closed before the console event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForConsoleMessage(options: WaitForConsoleMessageOptions?, callback: () -> Unit): IConsoleMessage?

    /**
     * Performs action and waits for a new {@code Download}. If predicate is provided, it passes {@code Download} value into the
     * {@code predicate} function and waits for {@code predicate(download)} to return a truthy value. Will throw an error if the page is
     * closed before the download event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForDownload(callback: () -> Unit): IDownload? {
        return waitForDownload(null, callback)
    }

    /**
     * Performs action and waits for a new {@code Download}. If predicate is provided, it passes {@code Download} value into the
     * {@code predicate} function and waits for {@code predicate(download)} to return a truthy value. Will throw an error if the page is
     * closed before the download event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForDownload(options: WaitForDownloadOptions?, callback: () -> Unit): IDownload?

    /**
     * Performs action and waits for a new {@code FileChooser} to be created. If predicate is provided, it passes {@code FileChooser} value
     * into the {@code predicate} function and waits for {@code predicate(fileChooser)} to return a truthy value. Will throw an error if
     * the page is closed before the file chooser is opened.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForFileChooser(callback: () -> Unit): IFileChooser? {
        return waitForFileChooser(null, callback)
    }

    /**
     * Performs action and waits for a new {@code FileChooser} to be created. If predicate is provided, it passes {@code FileChooser} value
     * into the {@code predicate} function and waits for {@code predicate(fileChooser)} to return a truthy value. Will throw an error if
     * the page is closed before the file chooser is opened.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForFileChooser(options: WaitForFileChooserOptions?, callback: () -> Unit): IFileChooser?

    /**
     * Returns when the {@code expression} returns a truthy value. It resolves to a JSHandle of the truthy value.
     *
     * <p> The {@link Page#waitForFunction Page.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType webkit = playwright.webkit();
     *       Browser browser = webkit.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50,  50);
     *       page.waitForFunction("() => window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@link Page#waitForFunction Page.waitForFunction()} function:
     * <pre>{@code
     * String selector = ".foo";
     * page.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForFunction Frame.waitForFunction()}.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun waitForFunction(expression: String): IJSHandle {
        return waitForFunction(expression, null)
    }

    /**
     * Returns when the {@code expression} returns a truthy value. It resolves to a JSHandle of the truthy value.
     *
     * <p> The {@link Page#waitForFunction Page.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType webkit = playwright.webkit();
     *       Browser browser = webkit.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50,  50);
     *       page.waitForFunction("() => window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@link Page#waitForFunction Page.waitForFunction()} function:
     * <pre>{@code
     * String selector = ".foo";
     * page.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForFunction Frame.waitForFunction()}.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun waitForFunction(expression: String, arg: Any?): IJSHandle {
        return waitForFunction(expression, arg, null)
    }

    /**
     * Returns when the {@code expression} returns a truthy value. It resolves to a JSHandle of the truthy value.
     *
     * <p> The {@link Page#waitForFunction Page.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType webkit = playwright.webkit();
     *       Browser browser = webkit.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50,  50);
     *       page.waitForFunction("() => window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@link Page#waitForFunction Page.waitForFunction()} function:
     * <pre>{@code
     * String selector = ".foo";
     * page.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForFunction Frame.waitForFunction()}.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun waitForFunction(expression: String, arg: Any?, options: WaitForFunctionOptions?): IJSHandle

    /**
     * Returns when the required load state has been reached.
     *
     * <p> This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * page.click("button"); // Click triggers navigation.
     * page.waitForLoadState(); // The promise resolves after "load" event.
     * }</pre>
     * <pre>{@code
     * Page popup = page.waitForPopup(() -> {
     *   page.click("button"); // Click triggers a popup.
     * });
     * popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
     * System.out.println(popup.title()); // Popup is ready to use.
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForLoadState Frame.waitForLoadState()}.
     */
    fun waitForLoadState() {
        waitForLoadState(null)
    }

    /**
     * Returns when the required load state has been reached.
     *
     * <p> This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * page.click("button"); // Click triggers navigation.
     * page.waitForLoadState(); // The promise resolves after "load" event.
     * }</pre>
     * <pre>{@code
     * Page popup = page.waitForPopup(() -> {
     *   page.click("button"); // Click triggers a popup.
     * });
     * popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
     * System.out.println(popup.title()); // Popup is ready to use.
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForLoadState Frame.waitForLoadState()}.
     *
     * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
     * document, the method resolves immediately. Can be one of:
     * <ul>
     * <li> {@code "load"} - wait for the {@code load} event to be fired.</li>
     * <li> {@code "domcontentloaded"} - wait for the {@code DOMContentLoaded} event to be fired.</li>
     * <li> {@code "networkidle"} - wait until there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    fun waitForLoadState(state: LoadState?) {
        return waitForLoadState(state, null)
    }

    /**
     * Returns when the required load state has been reached.
     *
     * <p> This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * page.click("button"); // Click triggers navigation.
     * page.waitForLoadState(); // The promise resolves after "load" event.
     * }</pre>
     * <pre>{@code
     * Page popup = page.waitForPopup(() -> {
     *   page.click("button"); // Click triggers a popup.
     * });
     * popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
     * System.out.println(popup.title()); // Popup is ready to use.
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForLoadState Frame.waitForLoadState()}.
     *
     * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
     * document, the method resolves immediately. Can be one of:
     * <ul>
     * <li> {@code "load"} - wait for the {@code load} event to be fired.</li>
     * <li> {@code "domcontentloaded"} - wait for the {@code DOMContentLoaded} event to be fired.</li>
     * <li> {@code "networkidle"} - wait until there are no network connections for at least {@code 500} ms.</li>
     * </ul>
     */
    fun waitForLoadState(state: LoadState?, options: WaitForLoadStateOptions?)

    /**
     * Waits for the main frame navigation and returns the main resource response. In case of multiple redirects, the
     * navigation will resolve with the response of the last redirect. In case of navigation to a different anchor or
     * navigation due to History API usage, the navigation will resolve with {@code null}.
     *
     * <p> This resolves when the page navigates to a new URL or reloads. It is useful for when you run code which will indirectly
     * cause the page to navigate. e.g. The click target has an {@code onclick} handler that triggers navigation from a {@code setTimeout}.
     * Consider this example:
     * <pre>{@code
     * // The method returns after navigation has finished
     * Response response = page.waitForNavigation(() -> {
     *   page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * });
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Usage of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API</a> to change the URL is
     * considered a navigation.
     *
     * <p> Shortcut for main frame's {@link Frame#waitForNavigation Frame.waitForNavigation()}.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForNavigation(callback: () -> Unit): IResponse? {
        return waitForNavigation(null, callback)
    }

    /**
     * Waits for the main frame navigation and returns the main resource response. In case of multiple redirects, the
     * navigation will resolve with the response of the last redirect. In case of navigation to a different anchor or
     * navigation due to History API usage, the navigation will resolve with {@code null}.
     *
     * <p> This resolves when the page navigates to a new URL or reloads. It is useful for when you run code which will indirectly
     * cause the page to navigate. e.g. The click target has an {@code onclick} handler that triggers navigation from a {@code setTimeout}.
     * Consider this example:
     * <pre>{@code
     * // The method returns after navigation has finished
     * Response response = page.waitForNavigation(() -> {
     *   page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * });
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Usage of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API</a> to change the URL is
     * considered a navigation.
     *
     * <p> Shortcut for main frame's {@link Frame#waitForNavigation Frame.waitForNavigation()}.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForNavigation(options: WaitForNavigationOptions?, callback: () -> Unit): IResponse?

    /**
     * Performs action and waits for a popup {@code Page}. If predicate is provided, it passes [Popup] value into the {@code predicate}
     * function and waits for {@code predicate(page)} to return a truthy value. Will throw an error if the page is closed before the
     * popup event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForPopup(callback: () -> Unit): IPage? {
        return waitForPopup(null, callback)
    }

    /**
     * Performs action and waits for a popup {@code Page}. If predicate is provided, it passes [Popup] value into the {@code predicate}
     * function and waits for {@code predicate(page)} to return a truthy value. Will throw an error if the page is closed before the
     * popup event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForPopup(options: WaitForPopupOptions?, callback: () -> Unit): IPage?

    /**
     * Waits for the matching request and returns it.  See <a
     * href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Request request = page.waitForRequest("https://example.com/resource", () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     *
     * // Waits for the next request matching some conditions
     * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object.
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForRequest(urlOrPredicate: String, callback: () -> Unit): IRequest? {
        return waitForRequest(urlOrPredicate, null, callback)
    }

    /**
     * Waits for the matching request and returns it.  See <a
     * href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Request request = page.waitForRequest("https://example.com/resource", () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     *
     * // Waits for the next request matching some conditions
     * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object.
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForRequest(urlOrPredicate: String?, options: WaitForRequestOptions?, callback: () -> Unit): IRequest?

    /**
     * Waits for the matching request and returns it.  See <a
     * href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Request request = page.waitForRequest("https://example.com/resource", () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     *
     * // Waits for the next request matching some conditions
     * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object.
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForRequest(urlOrPredicate: Pattern, callback: () -> Unit): IRequest? {
        return waitForRequest(urlOrPredicate, null, callback)
    }

    /**
     * Waits for the matching request and returns it.  See <a
     * href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Request request = page.waitForRequest("https://example.com/resource", () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     *
     * // Waits for the next request matching some conditions
     * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForRequest(urlOrPredicate: Pattern, options: WaitForRequestOptions?, callback: () -> Unit): IRequest?

    /**
     * Waits for the matching request and returns it.  See <a
     * href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Request request = page.waitForRequest("https://example.com/resource", () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     *
     * // Waits for the next request matching some conditions
     * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForRequest(urlOrPredicate: ((IRequest) -> Boolean), callback: () -> Unit): IRequest? {
        return waitForRequest(urlOrPredicate, null, callback)
    }

    /**
     * Waits for the matching request and returns it.  See <a
     * href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Request request = page.waitForRequest("https://example.com/resource", () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     *
     * // Waits for the next request matching some conditions
     * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
     *   // Triggers the request
     *   page.click("button.triggers-request");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForRequest(
        urlOrPredicate: ((IRequest) -> Boolean)?,
        options: WaitForRequestOptions?,
        callback: () -> Unit
    ): IRequest?

    /**
     * Returns the matched response. See <a href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for
     * event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Response response = page.waitForResponse("https://example.com/resource", () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     *
     * // Waits for the next response matching some conditions
     * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForResponse(urlOrPredicate: String, callback: () -> Unit): IResponse? {
        return waitForResponse(urlOrPredicate, null, callback)
    }

    /**
     * Returns the matched response. See <a href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for
     * event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Response response = page.waitForResponse("https://example.com/resource", () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     *
     * // Waits for the next response matching some conditions
     * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForResponse(urlOrPredicate: String?, options: WaitForResponseOptions?, callback: () -> Unit): IResponse?

    /**
     * Returns the matched response. See <a href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for
     * event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Response response = page.waitForResponse("https://example.com/resource", () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     *
     * // Waits for the next response matching some conditions
     * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForResponse(urlOrPredicate: Pattern, callback: () -> Unit): IResponse? {
        return waitForResponse(urlOrPredicate, null, callback)
    }

    /**
     * Returns the matched response. See <a href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for
     * event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Response response = page.waitForResponse("https://example.com/resource", () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     *
     * // Waits for the next response matching some conditions
     * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForResponse(urlOrPredicate: Pattern?, options: WaitForResponseOptions?, callback: () -> Unit): IResponse?

    /**
     * Returns the matched response. See <a href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for
     * event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Response response = page.waitForResponse("https://example.com/resource", () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     *
     * // Waits for the next response matching some conditions
     * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForResponse(urlOrPredicate: ((IResponse) -> Boolean), callback: () -> Unit): IResponse? {
        return waitForResponse(urlOrPredicate, null, callback)
    }

    /**
     * Returns the matched response. See <a href="https://playwright.dev/java/docs/events/#waiting-for-event">waiting for
     * event</a> for more details about events.
     * <pre>{@code
     * // Waits for the next response with the specified url
     * Response response = page.waitForResponse("https://example.com/resource", () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     *
     * // Waits for the next response matching some conditions
     * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
     *   // Triggers the response
     *   page.click("button.triggers-response");
     * });
     * }</pre>
     *
     * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object.
     * @param callback       Callback that performs the action triggering the event.
     */
    fun waitForResponse(
        urlOrPredicate: ((IResponse) -> Boolean)?,
        options: WaitForResponseOptions?,
        callback: () -> Unit
    ): IResponse?

    /**
     * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
     * {@code detached}.
     *
     * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at
     * the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the
     * selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
     *
     * <p> This method works across navigations:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType chromium = playwright.chromium();
     *       Browser browser = chromium.launch();
     *       Page page = browser.newPage();
     *       for (String currentURL : Arrays.asList("https://google.com", "https://bbc.com")) {
     *         page.navigate(currentURL);
     *         ElementHandle element = page.waitForSelector("img");
     *         System.out.println("Loaded image: " + element.getAttribute("src"));
     *       }
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     *                 details.
     */
    fun waitForSelector(selector: String): IElementHandle? {
        return waitForSelector(selector, null)
    }

    /**
     * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
     * {@code detached}.
     *
     * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at
     * the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the
     * selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
     *
     * <p> This method works across navigations:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType chromium = playwright.chromium();
     *       Browser browser = chromium.launch();
     *       Page page = browser.newPage();
     *       for (String currentURL : Arrays.asList("https://google.com", "https://bbc.com")) {
     *         page.navigate(currentURL);
     *         ElementHandle element = page.waitForSelector("img");
     *         System.out.println("Loaded image: " + element.getAttribute("src"));
     *       }
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     *                 details.
     */
    fun waitForSelector(selector: String, options: WaitForSelectorOptions?): IElementHandle?

    /**
     * Waits for the given {@code timeout} in milliseconds.
     *
     * <p> Note that {@code page.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going to be
     * flaky. Use signals such as network events, selectors becoming visible and others instead.
     * <pre>{@code
     * // wait for 1 second
     * page.waitForTimeout(1000);
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForTimeout Frame.waitForTimeout()}.
     *
     * @param timeout A timeout to wait for
     */
    fun waitForTimeout(timeout: Double)

    /**
     * Waits for the main frame to navigate to the given URL.
     * <pre>{@code
     * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * page.waitForURL("**\/target.html");
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForURL Frame.waitForURL()}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: String) {
        waitForURL(url, null)
    }

    /**
     * Waits for the main frame to navigate to the given URL.
     * <pre>{@code
     * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * page.waitForURL("**\/target.html");
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForURL Frame.waitForURL()}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: String, options: WaitForURLOptions?)

    /**
     * Waits for the main frame to navigate to the given URL.
     * <pre>{@code
     * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * page.waitForURL("**\/target.html");
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForURL Frame.waitForURL()}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: Pattern) {
        waitForURL(url, null)
    }

    /**
     * Waits for the main frame to navigate to the given URL.
     * <pre>{@code
     * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * page.waitForURL("**\/target.html");
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForURL Frame.waitForURL()}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: Pattern, options: WaitForURLOptions?)

    /**
     * Waits for the main frame to navigate to the given URL.
     * <pre>{@code
     * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * page.waitForURL("**\/target.html");
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForURL Frame.waitForURL()}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: (String) -> Boolean) {
        waitForURL(url, null)
    }

    /**
     * Waits for the main frame to navigate to the given URL.
     * <pre>{@code
     * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * page.waitForURL("**\/target.html");
     * }</pre>
     *
     * <p> Shortcut for main frame's {@link Frame#waitForURL Frame.waitForURL()}.
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: (String) -> Boolean, options: WaitForURLOptions?)

    /**
     * Performs action and waits for a new {@code WebSocket}. If predicate is provided, it passes {@code WebSocket} value into the
     * {@code predicate} function and waits for {@code predicate(webSocket)} to return a truthy value. Will throw an error if the page is
     * closed before the WebSocket event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForWebSocket(callback: () -> Unit): IWebSocket? {
        return waitForWebSocket(null, callback)
    }

    /**
     * Performs action and waits for a new {@code WebSocket}. If predicate is provided, it passes {@code WebSocket} value into the
     * {@code predicate} function and waits for {@code predicate(webSocket)} to return a truthy value. Will throw an error if the page is
     * closed before the WebSocket event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForWebSocket(options: WaitForWebSocketOptions?, callback: () -> Unit): IWebSocket?

    /**
     * Performs action and waits for a new {@code Worker}. If predicate is provided, it passes {@code Worker} value into the {@code predicate}
     * function and waits for {@code predicate(worker)} to return a truthy value. Will throw an error if the page is closed before
     * the worker event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForWorker(callback: () -> Unit): IWorker? {
        return waitForWorker(null, callback)
    }

    /**
     * Performs action and waits for a new {@code Worker}. If predicate is provided, it passes {@code Worker} value into the {@code predicate}
     * function and waits for {@code predicate(worker)} to return a truthy value. Will throw an error if the page is closed before
     * the worker event is fired.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForWorker(options: WaitForWorkerOptions?, callback: () -> Unit): IWorker?

    /**
     * This method returns all of the dedicated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">WebWorkers</a> associated with the page.
     *
     * <p> <strong>NOTE:</strong> This does not contain ServiceWorkers
     */
    fun workers(): List<IWorker>

    /**
     * Adds one-off {@code Dialog} handler. The handler will be removed immediately after next {@code Dialog} is created.
     * <pre>{@code
     * page.onceDialog(dialog -> {
     *   dialog.accept("foo");
     * });
     *
     * // prints 'foo'
     * System.out.println(page.evaluate("prompt('Enter string:')"));
     *
     * // prints 'null' as the dialog will be auto-dismissed because there are no handlers.
     * System.out.println(page.evaluate("prompt('Enter string:')"));
     * }</pre>
     *
     * <p> This code above is equivalent to:
     * <pre>{@code
     * Consumer<Dialog> handler = new Consumer<Dialog>() {
     *   @Override
     *   public void accept(Dialog dialog) {
     *     dialog.accept("foo");
     *     page.offDialog(this);
     *   }
     * };
     * page.onDialog(handler);
     *
     * // prints 'foo'
     * System.out.println(page.evaluate("prompt('Enter string:')"));
     *
     * // prints 'null' as the dialog will be auto-dismissed because there are no handlers.
     * System.out.println(page.evaluate("prompt('Enter string:')"));
     * }</pre>
     *
     * @param handler Receives the {@code Dialog} object, it **must** either {@link Dialog#accept Dialog.accept()} or {@link Dialog#dismiss
     *                Dialog.dismiss()} the dialog - otherwise the page will <a
     *                href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/EventLoop#never_blocking">freeze</a> waiting for the
     *                dialog, and actions like click will never finish.
     */
    fun onceDialog(handler: (IDialog) -> Unit)
}