package com.playwright.remote.engine.page.api

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
import com.playwright.remote.engine.options.element.ClickOptions
import com.playwright.remote.engine.options.element.DoubleClickOptions
import com.playwright.remote.engine.options.element.FillOptions
import com.playwright.remote.engine.options.element.HoverOptions
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.websocket.api.IWebSocket
import com.playwright.remote.engine.worker.api.IWorker
import java.nio.file.Path
import java.util.regex.Pattern

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
    fun close() {
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
}