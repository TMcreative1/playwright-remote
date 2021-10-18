package io.github.tmcreative1.playwright.remote.engine.frame.api

import io.github.tmcreative1.playwright.remote.core.enums.LoadState
import io.github.tmcreative1.playwright.remote.domain.file.FilePayload
import io.github.tmcreative1.playwright.remote.engine.frame.locator.api.ILocator
import io.github.tmcreative1.playwright.remote.engine.handle.element.api.IElementHandle
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import io.github.tmcreative1.playwright.remote.engine.options.*
import io.github.tmcreative1.playwright.remote.engine.options.element.*
import io.github.tmcreative1.playwright.remote.engine.options.element.PressOptions
import io.github.tmcreative1.playwright.remote.engine.options.element.TypeOptions
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForFunctionOptions
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForLoadStateOptions
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForNavigationOptions
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForURLOptions
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.route.UrlMatcher
import io.github.tmcreative1.playwright.remote.engine.route.response.api.IResponse
import java.nio.file.Path
import java.util.regex.Pattern

/**
 * At every point of time, page exposes its current frame tree via the {@link Page#mainFrame Page.mainFrame()} and {@link
 * Frame#childFrames Frame.childFrames()} methods.
 *
 * <p> {@code Frame} object's lifecycle is controlled by three events, dispatched on the page object:
 * <ul>
 * <li> {@link Page#onFrameAttached Page.onFrameAttached()} - fired when the frame gets attached to the page. A Frame can be
 * attached to the page only once.</li>
 * <li> {@link Page#onFrameNavigated Page.onFrameNavigated()} - fired when the frame commits navigation to a different URL.</li>
 * <li> {@link Page#onFrameDetached Page.onFrameDetached()} - fired when the frame gets detached from the page.  A Frame can be
 * detached from the page only once.</li>
 * </ul>
 *
 * <p> An example of dumping frame tree:
 * <pre>{@code
 * import com.microsoft.playwright.*;
 *
 * public class Example {
 *   public static void main(String[] args) {
 *     try (Playwright playwright = Playwright.create()) {
 *       BrowserType firefox = playwright.firefox();
 *       Browser browser = firefox.launch();
 *       Page page = browser.newPage();
 *       page.navigate("https://www.google.com/chrome/browser/canary.html");
 *       dumpFrameTree(page.mainFrame(), "");
 *       browser.close();
 *     }
 *   }
 *   static void dumpFrameTree(Frame frame, String indent) {
 *     System.out.println(indent + frame.url());
 *     for (Frame child : frame.childFrames()) {
 *       dumpFrameTree(child, indent + "  ");
 *     }
 *   }
 * }
 * }</pre>
 */
interface IFrame {

    /**
     * Returns frame's name attribute as specified in the tag.
     *
     * <p> If the name is empty, returns the id attribute instead.
     *
     * <p> <strong>NOTE:</strong> This value is calculated once when the frame is created, and will not update if the attribute is changed later.
     */
    fun name(): String

    /**
     * Returns frame's url.
     */
    fun url(): String

    /**
     * Returns the page containing this frame.
     */
    fun page(): IPage?

    /**
     * Returns the added tag when the script's onload fires or when the script content was injected into frame.
     *
     * <p> Adds a {@code <script>} tag into the page with the desired url or content.
     */
    fun addScriptTag(): IElementHandle = addScriptTag(null)

    /**
     * Returns the added tag when the script's onload fires or when the script content was injected into frame.
     *
     * <p> Adds a {@code <script>} tag into the page with the desired url or content.
     */
    fun addScriptTag(options: AddScriptTagOptions?): IElementHandle

    /**
     * Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
     *
     * <p> Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the
     * content.
     */
    fun addStyleTag(): IElementHandle = addStyleTag(null)

    /**
     * Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
     *
     * <p> Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the
     * content.
     */
    fun addStyleTag(options: AddStyleTagOptions?): IElementHandle

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
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun check(selector: String) = check(selector, null)

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
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun click(selector: String) = click(selector, null)

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
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun click(selector: String, options: ClickOptions?)

    /**
     * Gets the full HTML contents of the frame, including the doctype.
     */
    fun content(): String

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
     * <p> <strong>NOTE:</strong> {@code frame.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun doubleClick(selector: String) = doubleClick(selector, null)

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
     * <p> <strong>NOTE:</strong> {@code frame.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
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
     * frame.dispatchEvent("button#submit", "click");
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
     * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
     * Map<String, Object> arg = new HashMap<>();
     * arg.put("dataTransfer", dataTransfer);
     * frame.dispatchEvent("#source", "dragstart", arg);
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     * @param eventInit Optional event-specific initialization properties.
     */
    fun dispatchEvent(selector: String, type: String) =
        dispatchEvent(selector, type, null)

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * frame.dispatchEvent("button#submit", "click");
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
     * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
     * Map<String, Object> arg = new HashMap<>();
     * arg.put("dataTransfer", dataTransfer);
     * frame.dispatchEvent("#source", "dragstart", arg);
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     * @param eventInit Optional event-specific initialization properties.
     */
    fun dispatchEvent(selector: String, type: String, eventInit: Any?) =
        dispatchEvent(selector, type, eventInit, null)

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * frame.dispatchEvent("button#submit", "click");
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
     * JSHandle dataTransfer = frame.evaluateHandle("() => new DataTransfer()");
     * Map<String, Object> arg = new HashMap<>();
     * arg.put("dataTransfer", dataTransfer);
     * frame.dispatchEvent("#source", "dragstart", arg);
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     * @param eventInit Optional event-specific initialization properties.
     */
    fun dispatchEvent(selector: String, type: String, eventInit: Any?, options: DispatchEventOptions?)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to
     * {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If
     * no elements match the selector, the method throws an error.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evalOnSelector Frame.evalOnSelector()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * String searchValue = (String) frame.evalOnSelector("#search", "el => el.value");
     * String preloadHref = (String) frame.evalOnSelector("link[rel=preload]", "el => el.href");
     * String html = (String) frame.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evalOnSelector(selector: String, expression: String): Any =
        evalOnSelector(selector, expression, null)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to
     * {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If
     * no elements match the selector, the method throws an error.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evalOnSelector Frame.evalOnSelector()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * String searchValue = (String) frame.evalOnSelector("#search", "el => el.value");
     * String preloadHref = (String) frame.evalOnSelector("link[rel=preload]", "el => el.href");
     * String html = (String) frame.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evalOnSelector(selector: String, expression: String, arg: Any?): Any =
        evalOnSelector(selector, expression, arg, null)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds an element matching the specified selector within the frame and passes it as a first argument to
     * {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If
     * no elements match the selector, the method throws an error.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evalOnSelector Frame.evalOnSelector()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * String searchValue = (String) frame.evalOnSelector("#search", "el => el.value");
     * String preloadHref = (String) frame.evalOnSelector("link[rel=preload]", "el => el.href");
     * String html = (String) frame.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evalOnSelector(selector: String, expression: String, arg: Any?, options: EvalOnSelectorOptions?): Any

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds all elements matching the specified selector within the frame and passes an array of matched elements
     * as a first argument to {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with
     * selectors</a> for more details.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evalOnSelectorAll Frame.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * boolean divsCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evalOnSelectorAll(selector: String, expression: String): Any =
        evalOnSelectorAll(selector, expression, null)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds all elements matching the specified selector within the frame and passes an array of matched elements
     * as a first argument to {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with
     * selectors</a> for more details.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evalOnSelectorAll Frame.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * boolean divsCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
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
     * Returns the return value of {@code expression}.
     *
     * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evaluate Frame.evaluate()} would wait for the promise to resolve and return its value.
     *
     * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a non-[Serializable] value, then {@link
     * Frame#evaluate Frame.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional values that
     * are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
     * <pre>{@code
     * Object result = frame.evaluate("([x, y]) => {\n" +
     *   "  return Promise.resolve(x * y);\n" +
     *   "}", Arrays.asList(7, 8));
     * System.out.println(result); // prints "56"
     * }</pre>
     *
     * <p> A string can also be passed in instead of a function.
     * <pre>{@code
     * System.out.println(frame.evaluate("1 + 2")); // prints "3"
     * }</pre>
     *
     * <p> {@code ElementHandle} instances can be passed as an argument to the {@link Frame#evaluate Frame.evaluate()}:
     * <pre>{@code
     * ElementHandle bodyHandle = frame.querySelector("body");
     * String html = (String) frame.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
     * bodyHandle.dispose();
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluate(expression: String): Any =
        evaluate(expression, null)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evaluate Frame.evaluate()} would wait for the promise to resolve and return its value.
     *
     * <p> If the function passed to the {@link Frame#evaluate Frame.evaluate()} returns a non-[Serializable] value, then {@link
     * Frame#evaluate Frame.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional values that
     * are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
     * <pre>{@code
     * Object result = frame.evaluate("([x, y]) => {\n" +
     *   "  return Promise.resolve(x * y);\n" +
     *   "}", Arrays.asList(7, 8));
     * System.out.println(result); // prints "56"
     * }</pre>
     *
     * <p> A string can also be passed in instead of a function.
     * <pre>{@code
     * System.out.println(frame.evaluate("1 + 2")); // prints "3"
     * }</pre>
     *
     * <p> {@code ElementHandle} instances can be passed as an argument to the {@link Frame#evaluate Frame.evaluate()}:
     * <pre>{@code
     * ElementHandle bodyHandle = frame.querySelector("body");
     * String html = (String) frame.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
     * bodyHandle.dispose();
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluate(expression: String, arg: Any?): Any

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> The only difference between {@link Frame#evaluate Frame.evaluate()} and {@link Frame#evaluateHandle
     * Frame.evaluateHandle()} is that {@link Frame#evaluateHandle Frame.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function, passed to the {@link Frame#evaluateHandle Frame.evaluateHandle()}, returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evaluateHandle Frame.evaluateHandle()} would wait for the promise to resolve and return its value.
     * <pre>{@code
     * // Handle for the window object.
     * JSHandle aWindowHandle = frame.evaluateHandle("() => Promise.resolve(window)");
     * }</pre>
     *
     * <p> A string can also be passed in instead of a function.
     * <pre>{@code
     * JSHandle aHandle = frame.evaluateHandle("document"); // Handle for the "document".
     * }</pre>
     *
     * <p> {@code JSHandle} instances can be passed as an argument to the {@link Frame#evaluateHandle Frame.evaluateHandle()}:
     * <pre>{@code
     * JSHandle aHandle = frame.evaluateHandle("() => document.body");
     * JSHandle resultHandle = frame.evaluateHandle("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(aHandle, "hello"));
     * System.out.println(resultHandle.jsonValue());
     * resultHandle.dispose();
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluateHandle(expression: String): IJSHandle =
        evaluateHandle(expression, null)

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> The only difference between {@link Frame#evaluate Frame.evaluate()} and {@link Frame#evaluateHandle
     * Frame.evaluateHandle()} is that {@link Frame#evaluateHandle Frame.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function, passed to the {@link Frame#evaluateHandle Frame.evaluateHandle()}, returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Frame#evaluateHandle Frame.evaluateHandle()} would wait for the promise to resolve and return its value.
     * <pre>{@code
     * // Handle for the window object.
     * JSHandle aWindowHandle = frame.evaluateHandle("() => Promise.resolve(window)");
     * }</pre>
     *
     * <p> A string can also be passed in instead of a function.
     * <pre>{@code
     * JSHandle aHandle = frame.evaluateHandle("document"); // Handle for the "document".
     * }</pre>
     *
     * <p> {@code JSHandle} instances can be passed as an argument to the {@link Frame#evaluateHandle Frame.evaluateHandle()}:
     * <pre>{@code
     * JSHandle aHandle = frame.evaluateHandle("() => document.body");
     * JSHandle resultHandle = frame.evaluateHandle("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(aHandle, "hello"));
     * System.out.println(resultHandle.jsonValue());
     * resultHandle.dispose();
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluateHandle(expression: String, arg: Any?): IJSHandle

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the element, fills it and
     * triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input field.
     *
     * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
     * However, if the element is inside the {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
     * instead.
     *
     * <p> To send fine-grained keyboard events, use {@link Frame#type Frame.type()}.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
     */
    fun fill(selector: String, value: String) =
        fill(selector, value, null)

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the element, fills it and
     * triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input field.
     *
     * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
     * However, if the element is inside the {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
     * instead.
     *
     * <p> To send fine-grained keyboard events, use {@link Frame#type Frame.type()}.
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
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun focus(selector: String) =
        focus(selector, null)

    /**
     * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method
     * waits until a matching element appears in the DOM.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun focus(selector: String, options: FocusOptions?)

    /**
     * Returns element attribute value.
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param name Attribute name to get the value for.
     */
    fun getAttribute(selector: String, name: String): String? =
        getAttribute(selector, name, null)

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
     * last redirect.
     *
     * <p> {@code frame.goto} will throw an error if:
     * <ul>
     * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
     * <li> target URL is invalid.</li>
     * <li> the {@code timeout} is exceeded during navigation.</li>
     * <li> the remote server does not respond or is unreachable.</li>
     * <li> the main resource failed to load.</li>
     * </ul>
     *
     * <p> {@code frame.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404
     * "Not Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
     * Response#status Response.status()}.
     *
     * <p> <strong>NOTE:</strong> {@code frame.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
     * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
     *
     * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
     * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
     *
     * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
     */
    fun navigate(url: String): IResponse? = navigate(url, NavigateOptions {})

    /**
     * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
     * last redirect.
     *
     * <p> {@code frame.goto} will throw an error if:
     * <ul>
     * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
     * <li> target URL is invalid.</li>
     * <li> the {@code timeout} is exceeded during navigation.</li>
     * <li> the remote server does not respond or is unreachable.</li>
     * <li> the main resource failed to load.</li>
     * </ul>
     *
     * <p> {@code frame.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404
     * "Not Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
     * Response#status Response.status()}.
     *
     * <p> <strong>NOTE:</strong> {@code frame.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
     * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
     *
     * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
     * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
     *
     * @param url URL to navigate frame to. The url should include scheme, e.g. {@code https://}.
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
     * Returns {@code true} if the frame has been detached, or {@code false} otherwise.
     */
    fun isDetached(): Boolean

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

    /**
     * {@code key} can specify the intended <a
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
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(selector: String, key: String) {
        press(selector, key, null)
    }

    /**
     * {@code key} can specify the intended <a
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
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(selector: String, key: String, options: PressOptions?)

    /**
     * Returns the ElementHandle pointing to the frame element.
     *
     * <p> The method finds an element matching the specified selector within the frame. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
     * selector, returns {@code null}.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelector(selector: String?): IElementHandle? =
        querySelector(selector, null)

    /**
     * Returns the ElementHandle pointing to the frame element.
     *
     * <p> The method finds an element matching the specified selector within the frame. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
     * selector, returns {@code null}.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelector(selector: String?, options: QuerySelectorOptions?): IElementHandle?

    /**
     * Returns the ElementHandles pointing to the frame elements.
     *
     * <p> The method finds all elements matching the specified selector within the frame. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
     * selector, returns empty array.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelectorAll(selector: String?): List<IElementHandle>?

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: String?): List<String> {
        return selectOption(selector, value, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: String?, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: IElementHandle?): List<String> {
        return selectOption(selector, value, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: IElementHandle?, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<String>?): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<String>?, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: SelectOption?): List<String> {
        return selectOption(selector, value, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, value: SelectOption?, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<IElementHandle>): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<IElementHandle>?, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(selector: String, values: Array<SelectOption>): List<String> {
        return selectOption(selector, values, null)
    }

    /**
     * This method waits for an element matching {@code selector}, waits for <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until all specified options are
     * present in the {@code <select>} element and selects these options.
     *
     * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside the
     * {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
     * instead.
     *
     * <p> Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
     * <pre>{@code
     * // single selection matching the value
     * frame.selectOption("select#colors", "blue");
     * // single selection matching both the value and the label
     * frame.selectOption("select#colors", new SelectOption().setLabel("Blue"));
     * // multiple selection
     * frame.selectOption("select#colors", new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
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
     * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
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
     * <p> <strong>NOTE:</strong> {@code frame.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
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
     * Returns the page title.
     */
    fun title(): String

    /**
     * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code frame.type} can be used to
     * send fine-grained keyboard events. To fill values in form fields, use {@link Frame#fill Frame.fill()}.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
     * <pre>{@code
     * // Types instantly
     * frame.type("#mytextarea", "Hello");
     * // Types slower, like a user
     * frame.type("#mytextarea", "World", new Frame.TypeOptions().setDelay(100));
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param text A text to type into a focused element.
     */
    fun type(selector: String, text: String) {
        type(selector, text, null)
    }

    /**
     * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code frame.type} can be used to
     * send fine-grained keyboard events. To fill values in form fields, use {@link Frame#fill Frame.fill()}.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
     * <pre>{@code
     * // Types instantly
     * frame.type("#mytextarea", "Hello");
     * // Types slower, like a user
     * frame.type("#mytextarea", "World", new Frame.TypeOptions().setDelay(100));
     * }</pre>
     *
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param text A text to type into a focused element.
     */
    fun type(selector: String, text: String, options: TypeOptions?)

    /**
     * This method checks an element matching {@code selector} by performing the following steps:
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
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun uncheck(selector: String) {
        uncheck(selector, null)
    }

    /**
     * This method checks an element matching {@code selector} by performing the following steps:
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
     * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See <a
     * href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     */
    fun uncheck(selector: String, options: UncheckOptions?)

    /**
     * Returns when the {@code expression} returns a truthy value, returns that value.
     *
     * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType firefox = playwright.firefox();
     *       Browser browser = firefox.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50, 50);
     *       page.mainFrame().waitForFunction("window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
     * <pre>{@code
     * String selector = ".foo";
     * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun waitForFunction(expression: String): IJSHandle {
        return waitForFunction(expression, null)
    }

    /**
     * Returns when the {@code expression} returns a truthy value, returns that value.
     *
     * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType firefox = playwright.firefox();
     *       Browser browser = firefox.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50, 50);
     *       page.mainFrame().waitForFunction("window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
     * <pre>{@code
     * String selector = ".foo";
     * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun waitForFunction(expression: String, arg: Any?): IJSHandle {
        return waitForFunction(expression, arg, null)
    }

    /**
     * Returns when the {@code expression} returns a truthy value, returns that value.
     *
     * <p> The {@link Frame#waitForFunction Frame.waitForFunction()} can be used to observe viewport size change:
     * <pre>{@code
     * import com.microsoft.playwright.*;
     *
     * public class Example {
     *   public static void main(String[] args) {
     *     try (Playwright playwright = Playwright.create()) {
     *       BrowserType firefox = playwright.firefox();
     *       Browser browser = firefox.launch();
     *       Page page = browser.newPage();
     *       page.setViewportSize(50, 50);
     *       page.mainFrame().waitForFunction("window.innerWidth < 100");
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * <p> To pass an argument to the predicate of {@code frame.waitForFunction} function:
     * <pre>{@code
     * String selector = ".foo";
     * frame.waitForFunction("selector => !!document.querySelector(selector)", selector);
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun waitForFunction(expression: String, arg: Any?, options: WaitForFunctionOptions?): IJSHandle

    /**
     * Waits for the required load state to be reached.
     *
     * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * frame.click("button"); // Click triggers navigation.
     * frame.waitForLoadState(); // Waits for "load" state by default.
     * }</pre>
     */
    fun waitForLoadState() {
        waitForLoadState(null)
    }

    /**
     * Waits for the required load state to be reached.
     *
     * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * frame.click("button"); // Click triggers navigation.
     * frame.waitForLoadState(); // Waits for "load" state by default.
     * }</pre>
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
        waitForLoadState(state, null)
    }

    /**
     * Waits for the required load state to be reached.
     *
     * <p> This returns when the frame reaches a required load state, {@code load} by default. The navigation must have been committed
     * when this method is called. If current document has already reached the required state, resolves immediately.
     * <pre>{@code
     * frame.click("button"); // Click triggers navigation.
     * frame.waitForLoadState(); // Waits for "load" state by default.
     * }</pre>
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
     * Waits for the frame navigation and returns the main resource response. In case of multiple redirects, the navigation
     * will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to
     * History API usage, the navigation will resolve with {@code null}.
     *
     * <p> This method waits for the frame to navigate to a new URL. It is useful for when you run code which will indirectly cause
     * the frame to navigate. Consider this example:
     * <pre>{@code
     * // The method returns after navigation has finished
     * Response response = frame.waitForNavigation(() -> {
     *   // Clicking the link will indirectly cause a navigation
     *   frame.click("a.delayed-navigation");
     * });
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Usage of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API</a> to change the URL is
     * considered a navigation.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForNavigation(callback: () -> Unit): IResponse? {
        return waitForNavigation(null, callback)
    }

    /**
     * Waits for the frame navigation and returns the main resource response. In case of multiple redirects, the navigation
     * will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to
     * History API usage, the navigation will resolve with {@code null}.
     *
     * <p> This method waits for the frame to navigate to a new URL. It is useful for when you run code which will indirectly cause
     * the frame to navigate. Consider this example:
     * <pre>{@code
     * // The method returns after navigation has finished
     * Response response = frame.waitForNavigation(() -> {
     *   // Clicking the link will indirectly cause a navigation
     *   frame.click("a.delayed-navigation");
     * });
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> Usage of the <a href="https://developer.mozilla.org/en-US/docs/Web/API/History_API">History API</a> to change the URL is
     * considered a navigation.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForNavigation(options: WaitForNavigationOptions?, callback: () -> Unit): IResponse?

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
     *         ElementHandle element = page.mainFrame().waitForSelector("img");
     *         System.out.println("Loaded image: " + element.getAttribute("src"));
     *       }
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
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
     *         ElementHandle element = page.mainFrame().waitForSelector("img");
     *         System.out.println("Loaded image: " + element.getAttribute("src"));
     *       }
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun waitForSelector(selector: String, options: WaitForSelectorOptions?): IElementHandle? =
        waitForSelector(selector, options, false)

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
     *         ElementHandle element = page.mainFrame().waitForSelector("img");
     *         System.out.println("Loaded image: " + element.getAttribute("src"));
     *       }
     *       browser.close();
     *     }
     *   }
     * }
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun waitForSelector(selector: String, options: WaitForSelectorOptions?, omitReturnValue: Boolean): IElementHandle?

    /**
     * Waits for the given {@code timeout} in milliseconds.
     *
     * <p> Note that {@code frame.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going to
     * be flaky. Use signals such as network events, selectors becoming visible and others instead.
     *
     * @param timeout A timeout to wait for
     */
    fun waitForTimeout(timeout: Double)

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: String) {
        waitForURL(url, null)
    }

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: String, options: WaitForURLOptions?)

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waifForURL(url: Pattern) {
        waitForURL(url, null)
    }

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: Pattern, options: WaitForURLOptions?) =
        waitForURL(UrlMatcher(url), options)

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: (String) -> Boolean) {
        waitForURL(url, null)
    }

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(url: (String) -> Boolean, options: WaitForURLOptions?) =
        waitForURL(UrlMatcher(url), options)

    /**
     * Waits for the frame to navigate to the given URL.
     * <pre>{@code
     * frame.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
     * frame.waitForURL("**\/target.html");
     * }</pre>
     *
     * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    fun waitForURL(matcher: UrlMatcher, options: WaitForURLOptions?)

    fun dragAndDrop(source: String, target: String) = dragAndDrop(source, target, null)

    fun dragAndDrop(source: String, target: String, options: DragAndDropOptions?)

    /**
     * The method returns an element locator that can be used to perform actions in the frame. Locator is resolved to the
     * element immediately before performing an action, so a series of actions on the same locator can in fact be performed on
     * different DOM elements. That would happen if the DOM structure between those actions has changed.
     *
     * <p> Note that locator always implies visibility, so it will always be locating visible elements.
     *
     * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors/">working with
     * selectors</a> for more details.
     */
    fun locator(selector: String): ILocator

    /**
     * This method checks or unchecks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws.</li>
     * <li> If the element already has the right checked state, this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now checked or unchecked. If not, this method throws.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
     * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param checked Whether to check or uncheck the checkbox.
     */
    fun setChecked(selector: String, checked: Boolean) =
        setChecked(selector, checked, null)

    /**
     * This method checks or unchecks an element matching {@code selector} by performing the following steps:
     * <ol>
     * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
     * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws.</li>
     * <li> If the element already has the right checked state, this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the matched element,
     * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now checked or unchecked. If not, this method throws.</li>
     * </ol>
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. See
     * <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more details.
     * @param checked Whether to check or uncheck the checkbox.
     */
    fun setChecked(selector: String, checked: Boolean, options: SetCheckedOptions?)
}