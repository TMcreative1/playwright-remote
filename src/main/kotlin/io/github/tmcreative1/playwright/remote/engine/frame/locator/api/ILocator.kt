package io.github.tmcreative1.playwright.remote.engine.frame.locator.api

import io.github.tmcreative1.playwright.remote.domain.BoundingBox
import io.github.tmcreative1.playwright.remote.domain.file.FilePayload
import io.github.tmcreative1.playwright.remote.engine.handle.element.api.IElementHandle
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import io.github.tmcreative1.playwright.remote.engine.options.*
import io.github.tmcreative1.playwright.remote.engine.options.element.*
import io.github.tmcreative1.playwright.remote.engine.options.element.PressOptions
import io.github.tmcreative1.playwright.remote.engine.options.element.ScreenshotOptions
import io.github.tmcreative1.playwright.remote.engine.options.element.TypeOptions
import java.nio.file.Path

interface ILocator {
    /**
     * Returns an array of {@code node.innerText} values for all matching nodes.
     */
    fun allInnerTexts(): List<String>

    /**
     * Returns an array of {@code node.textContent} values for all matching nodes.
     */
    fun allTextContents(): List<String>

    /**
     * This method returns the bounding box of the element, or {@code null} if the element is not visible. The bounding box is
     * calculated relative to the main frame viewport - which is usually the same as the browser window.
     *
     * <p> Scrolling affects the returned bonding box, similarly to <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect">Element.getBoundingClientRect</a>.
     * That means {@code x} and/or {@code y} may be negative.
     *
     * <p> Elements from child frames return the bounding box relative to the main frame, unlike the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect">Element.getBoundingClientRect</a>.
     *
     * <p> Assuming the page is static, it is safe to use bounding box coordinates to perform input. For example, the following
     * snippet should click the center of the element.
     * <pre>{@code
     * BoundingBox box = element.boundingBox();
     * page.mouse().click(box.x + box.width / 2, box.y + box.height / 2);
     * }</pre>
     */
    fun boundingBox(): BoundingBox = boundingBox(null)

    /**
     * This method returns the bounding box of the element, or {@code null} if the element is not visible. The bounding box is
     * calculated relative to the main frame viewport - which is usually the same as the browser window.
     *
     * <p> Scrolling affects the returned bonding box, similarly to <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect">Element.getBoundingClientRect</a>.
     * That means {@code x} and/or {@code y} may be negative.
     *
     * <p> Elements from child frames return the bounding box relative to the main frame, unlike the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Element/getBoundingClientRect">Element.getBoundingClientRect</a>.
     *
     * <p> Assuming the page is static, it is safe to use bounding box coordinates to perform input. For example, the following
     * snippet should click the center of the element.
     * <pre>{@code
     * BoundingBox box = element.boundingBox();
     * page.mouse().click(box.x + box.width / 2, box.y + box.height / 2);
     * }</pre>
     */
    fun boundingBox(options: BoundingBoxOptions?): BoundingBox

    /**
     * This method checks the element by performing the following steps:
     * <ol>
     * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already checked, this
     * method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now checked. If not, this method throws.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     */
    fun check() = check(null)

    /**
     * This method checks the element by performing the following steps:
     * <ol>
     * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already checked, this
     * method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now checked. If not, this method throws.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     */
    fun check(options: CheckOptions?)

    /**
     * This method clicks the element by performing the following steps:
     * <ol>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     */
    fun click() = click(null)

    /**
     * This method clicks the element by performing the following steps:
     * <ol>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     */
    fun click(options: ClickOptions?)

    /**
     * Returns the number of elements matching given selector.
     */
    fun count(): Int

    /**
     * This method double clicks the element by performing the following steps:
     * <ol>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first
     * click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@code element.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
     */
    fun doubleClick() = doubleClick(null)

    /**
     * This method double clicks the element by performing the following steps:
     * <ol>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first
     * click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@code element.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
     */
    fun doubleClick(options: DoubleClickOptions?)

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * element.dispatchEvent("click");
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
     * element.dispatchEvent("dragstart", arg);
     * }</pre>
     *
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     */
    fun dispatchEvent(type: String) = dispatchEvent(type, null)

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * element.dispatchEvent("click");
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
     * element.dispatchEvent("dragstart", arg);
     * }</pre>
     *
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     * @param eventInit Optional event-specific initialization properties.
     */
    fun dispatchEvent(type: String, eventInit: Any?) = dispatchEvent(type, eventInit, null)

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * element.dispatchEvent("click");
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
     * element.dispatchEvent("dragstart", arg);
     * }</pre>
     *
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     * @param eventInit Optional event-specific initialization properties.
     */
    fun dispatchEvent(type: String, eventInit: Any?, options: DispatchEventOptions?)

    /**
     * Resolves given locator to the first matching DOM element. If no elements matching the query are visible, waits for them
     * up to a given timeout. If multiple elements match the selector, throws.
     */
    fun elementHandle(): IElementHandle? = elementHandle(null)

    /**
     * Resolves given locator to the first matching DOM element. If no elements matching the query are visible, waits for them
     * up to a given timeout. If multiple elements match the selector, throws.
     */
    fun elementHandle(options: ElementHandleOptions?): IElementHandle?

    /**
     * Resolves given locator to all matching DOM elements.
     */
    fun elementHandles(): List<IElementHandle>?

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> This method passes this handle as the first argument to {@code expression}.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then
     * {@code handle.evaluate} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * Locator tweets = page.locator(".tweet .retweets");
     * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluate(expression: String): Any = evaluate(expression, null)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> This method passes this handle as the first argument to {@code expression}.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then
     * {@code handle.evaluate} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * Locator tweets = page.locator(".tweet .retweets");
     * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluate(expression: String, arg: Any?): Any = evaluate(expression, arg, null)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> This method passes this handle as the first argument to {@code expression}.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then
     * {@code handle.evaluate} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * Locator tweets = page.locator(".tweet .retweets");
     * assertEquals("10 retweets", tweets.evaluate("node => node.innerText"));
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluate(expression: String, arg: Any?, options: EvaluateOptions?): Any

    /**
     * The method finds all elements matching the specified locator and passes an array of matched elements as a first argument
     * to {@code expression}. Returns the result of {@code expression} invocation.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Locator#evaluateAll Locator.evaluateAll()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * Locator elements = page.locator("div");
     * boolean divCounts = (boolean) elements.evaluateAll("(divs, min) => divs.length >= min", 10);
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluateAll(expression: String): Any = evaluateAll(expression, null)

    /**
     * The method finds all elements matching the specified locator and passes an array of matched elements as a first argument
     * to {@code expression}. Returns the result of {@code expression} invocation.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Locator#evaluateAll Locator.evaluateAll()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * Locator elements = page.locator("div");
     * boolean divCounts = (boolean) elements.evaluateAll("(divs, min) => divs.length >= min", 10);
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluateAll(expression: String, arg: Any?): Any

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> This method passes this handle as the first argument to {@code expression}.
     *
     * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
     * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function passed to the {@link Locator#evaluateHandle Locator.evaluateHandle()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Locator#evaluateHandle Locator.evaluateHandle()} would wait for the promise to resolve and return its value.
     *
     * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluateHandle(expression: String): Any = evaluateHandle(expression, null)

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> This method passes this handle as the first argument to {@code expression}.
     *
     * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
     * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function passed to the {@link Locator#evaluateHandle Locator.evaluateHandle()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Locator#evaluateHandle Locator.evaluateHandle()} would wait for the promise to resolve and return its value.
     *
     * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluateHandle(expression: String, arg: Any?): IJSHandle = evaluateHandle(expression, arg, null)

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> This method passes this handle as the first argument to {@code expression}.
     *
     * <p> The only difference between {@link Locator#evaluate Locator.evaluate()} and {@link Locator#evaluateHandle
     * Locator.evaluateHandle()} is that {@link Locator#evaluateHandle Locator.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function passed to the {@link Locator#evaluateHandle Locator.evaluateHandle()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Locator#evaluateHandle Locator.evaluateHandle()} would wait for the promise to resolve and return its value.
     *
     * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluateHandle(expression: String, arg: Any?, options: EvaluateHandleOptions?): IJSHandle

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the
     * element, fills it and triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input
     * field.
     *
     * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
     * However, if the element is inside the {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
     * instead.
     *
     * <p> To send fine-grained keyboard events, use {@link Locator#type Locator.type()}.
     *
     * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
     */
    fun fill(value: String) = fill(value, null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the
     * element, fills it and triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input
     * field.
     *
     * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
     * However, if the element is inside the {@code <label>} element that has an associated <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
     * instead.
     *
     * <p> To send fine-grained keyboard events, use {@link Locator#type Locator.type()}.
     *
     * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
     */
    fun fill(value: String, options: FillOptions?)

    /**
     * Returns locator to the first matching element.
     */
    fun first(): ILocator

    /**
     * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus</a> on the element.
     */
    fun focus() = focus(null)

    /**
     * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus</a> on the element.
     */
    fun focus(options: FocusOptions?)

    /**
     * Returns element attribute value.
     *
     * @param name Attribute name to get the value for.
     */
    fun getAttribute(name: String): String? = getAttribute(name, null)

    /**
     * Returns element attribute value.
     *
     * @param name Attribute name to get the value for.
     */
    fun getAttribute(name: String, options: GetAttributeOptions?): String?

    /**
     * This method hovers over the element by performing the following steps:
     * <ol>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     */
    fun hover() = hover(null)

    /**
     * This method hovers over the element by performing the following steps:
     * <ol>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     */
    fun hover(options: HoverOptions?)

    /**
     * Returns the {@code element.innerHTML}.
     */
    fun innerHTML(): String = innerHTML(null)

    /**
     * Returns the {@code element.innerHTML}.
     */
    fun innerHTML(options: InnerHTMLOptions?): String

    /**
     * Returns the {@code element.innerText}.
     */
    fun innerText(): String = innerText(null)

    /**
     * Returns the {@code element.innerText}.
     */
    fun innerText(options: InnerTextOptions?): String

    /**
     * Returns {@code input.value} for {@code <input>} or {@code <textarea>} or {@code <select>} element. Throws for non-input elements.
     */
    fun inputValue(): String = inputValue(null)

    /**
     * Returns {@code input.value} for {@code <input>} or {@code <textarea>} or {@code <select>} element. Throws for non-input elements.
     */
    fun inputValue(options: InputValueOptions?): String

    /**
     * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
     */
    fun isChecked(): Boolean = isChecked(null)

    /**
     * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
     */
    fun isChecked(options: IsCheckedOptions?): Boolean

    /**
     * Returns whether the element is disabled, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     */
    fun isDisabled(): Boolean = isDisabled(null)

    /**
     * Returns whether the element is disabled, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     */
    fun isDisabled(options: IsDisabledOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
     */
    fun isEditable(): Boolean = isEditable(null)

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
     */
    fun isEditable(options: IsEditableOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     */
    fun isEnabled(): Boolean = isEnabled(null)

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     */
    fun isEnabled(options: IsEnabledOptions?): Boolean

    /**
     * Returns whether the element is hidden, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.
     */
    fun isHidden(): Boolean = isHidden(null)

    /**
     * Returns whether the element is hidden, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.
     */
    fun isHidden(options: IsHiddenOptions?): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.
     */
    fun isVisible(): Boolean = isVisible(null)

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.
     */
    fun isVisible(options: IsVisibleOptions?): Boolean

    /**
     * Returns locator to the last matching element.
     */
    fun last(): ILocator

    /**
     * The method finds an element matching the specified selector in the {@code Locator}'s subtree. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details.
     *
     * @param selector A selector to use when resolving DOM element. See <a href="https://playwright.dev/java/docs/selectors/">working with
     * selectors</a> for more details.
     */
    fun locator(selector: String): ILocator

    /**
     * Returns locator to the n-th matching element.
     */
    fun nth(index: Int): ILocator

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
     *
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(key: String) = press(key, null)

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
     *
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(key: String, options: PressOptions?)

    /**
     * Returns the buffer with the captured screenshot.
     *
     * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then
     * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
     */
    fun screenshot(): ByteArray = screenshot(null)

    /**
     * Returns the buffer with the captured screenshot.
     *
     * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then
     * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
     */
    fun screenshot(options: ScreenshotOptions?): ByteArray

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then tries to
     * scroll element into view, unless it is completely visible as defined by <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">IntersectionObserver</a>'s {@code ratio}.
     */
    fun scrollIntoViewIfNeeded() = scrollIntoViewIfNeeded(null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then tries to
     * scroll element into view, unless it is completely visible as defined by <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">IntersectionObserver</a>'s {@code ratio}.
     */
    fun scrollIntoViewIfNeeded(options: ScrollIntoViewIfNeededOptions?)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param value Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: String): List<String> = selectOption(value, null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param value Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: String, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param value Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: IElementHandle): List<String> = selectOption(value, null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param value Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: IElementHandle, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<String>): List<String> = selectOption(values, null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<String>, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param value Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: SelectOption): List<String> = selectOption(value, null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param value Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: SelectOption, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<IElementHandle>): List<String> = selectOption(values, null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<IElementHandle>, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<SelectOption>): List<String> = selectOption(values, null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, waits until
     * all specified options are present in the {@code <select>} element and selects these options.
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
     * element.selectOption("blue");
     * // single selection matching the label
     * element.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * element.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<SelectOption>, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then focuses
     * the element and selects all its text content.
     */
    fun selectText() = selectText(null)

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then focuses
     * the element and selects all its text content.
     */
    fun selectText(options: SelectTextOptions?)

    /**
     * This method expects {@code element} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Path) = setInputFiles(files, null)

    /**
     * This method expects {@code element} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Path, options: SetInputFilesOptions?)

    /**
     * This method expects {@code element} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Array<Path>) = setInputFiles(files, null)

    /**
     * This method expects {@code element} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Array<Path>, options: SetInputFilesOptions?)

    /**
     * This method expects {@code element} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(file: FilePayload) = setInputFiles(file, null)

    /**
     * This method expects {@code element} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(file: FilePayload, options: SetInputFilesOptions?)

    /**
     * This method expects {@code element} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Array<FilePayload>) = setInputFiles(files, null)

    /**
     * This method expects {@code element} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Array<FilePayload>, options: SetInputFilesOptions?)

    /**
     * This method taps the element by performing the following steps:
     * <ol>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@code element.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
     */
    fun tap() = tap(null)

    /**
     * This method taps the element by performing the following steps:
     * <ol>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     *
     * <p> <strong>NOTE:</strong> {@code element.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
     */
    fun tap(options: TapOptions?)

    /**
     * Returns the {@code node.textContent}.
     */
    fun textContent(): String = textContent(null)

    /**
     * Returns the {@code node.textContent}.
     */
    fun textContent(options: TextContentOptions?): String

    /**
     * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Locator#press Locator.press()}.
     * <pre>{@code
     * element.type("Hello"); // Types instantly
     * element.type("World", new Locator.TypeOptions().setDelay(100)); // Types slower, like a user
     * }</pre>
     *
     * <p> An example of typing into a text field and then submitting the form:
     * <pre>{@code
     * Locator element = page.locator("input");
     * element.type("some text");
     * element.press("Enter");
     * }</pre>
     *
     * @param text A text to type into a focused element.
     */
    fun type(text: String) = type(text, null)

    /**
     * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Locator#press Locator.press()}.
     * <pre>{@code
     * element.type("Hello"); // Types instantly
     * element.type("World", new Locator.TypeOptions().setDelay(100)); // Types slower, like a user
     * }</pre>
     *
     * <p> An example of typing into a text field and then submitting the form:
     * <pre>{@code
     * Locator element = page.locator("input");
     * element.type("some text");
     * element.press("Enter");
     * }</pre>
     *
     * @param text A text to type into a focused element.
     */
    fun type(text: String, options: TypeOptions?)

    /**
     * This method checks the element by performing the following steps:
     * <ol>
     * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already unchecked,
     * this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     */
    fun uncheck() = uncheck(null)

    /**
     * This method checks the element by performing the following steps:
     * <ol>
     * <li> Ensure that element is a checkbox or a radio input. If not, this method throws. If the element is already unchecked,
     * this method returns immediately.</li>
     * <li> Wait for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks on the element, unless
     * {@code force} option is set.</li>
     * <li> Scroll the element into view if needed.</li>
     * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
     * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
     * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
     * </ol>
     *
     * <p> If the element is detached from the DOM at any moment during the action, this method throws.
     *
     * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code TimeoutError}. Passing
     * zero timeout disables this.
     */
    fun uncheck(options: UncheckOptions?)
}