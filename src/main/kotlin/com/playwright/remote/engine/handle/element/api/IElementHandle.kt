package com.playwright.remote.engine.handle.element.api

import com.playwright.remote.core.enums.ElementState
import com.playwright.remote.domain.BoundingBox
import com.playwright.remote.domain.file.FilePayload
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.options.CheckOptions
import com.playwright.remote.engine.options.SelectOption
import com.playwright.remote.engine.options.element.*
import java.nio.file.Path

interface IElementHandle : IJSHandle {
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
     * BoundingBox box = elementHandle.boundingBox();
     * page.mouse().click(box.x + box.width / 2, box.y + box.height / 2);
     * }</pre>
     */
    fun boundingBox(): BoundingBox?

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
    fun check() {
        check(null)
    }

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
    fun click() {
        click(null)
    }

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
     * Returns the content frame for element handles referencing iframe nodes, or {@code null} otherwise
     */
    fun contentFrame(): IFrame?

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
     * <p> <strong>NOTE:</strong> {@code elementHandle.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
     */
    fun doubleClick() {
        doubleClick(null)
    }

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
     * <p> <strong>NOTE:</strong> {@code elementHandle.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
     */
    fun doubleClick(options: DoubleClickOptions?)

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * elementHandle.dispatchEvent("click");
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
     * elementHandle.dispatchEvent("dragstart", arg);
     * }</pre>
     *
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     */
    fun dispatchEvent(type: String) {
        dispatchEvent(type, null)
    }

    /**
     * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
     * {@code click} is dispatched. This is equivalent to calling <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
     * <pre>{@code
     * elementHandle.dispatchEvent("click");
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
     * elementHandle.dispatchEvent("dragstart", arg);
     * }</pre>
     *
     * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
     * @param eventInit Optional event-specific initialization properties.
     */
    fun dispatchEvent(type: String, eventInit: Any?)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds an element matching the specified selector in the {@code ElementHandle}s subtree and passes it as a first
     * argument to {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more
     * details. If no elements match the selector, the method throws an error.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * ElementHandle#evalOnSelector ElementHandle.evalOnSelector()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * ElementHandle tweetHandle = page.querySelector(".tweet");
     * assertEquals("100", tweetHandle.evalOnSelector(".like", "node => node.innerText"));
     * assertEquals("10", tweetHandle.evalOnSelector(".retweets", "node => node.innerText"));
     * }</pre>
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
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds an element matching the specified selector in the {@code ElementHandle}s subtree and passes it as a first
     * argument to {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more
     * details. If no elements match the selector, the method throws an error.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * ElementHandle#evalOnSelector ElementHandle.evalOnSelector()} would wait for the promise to resolve and return its value.
     *
     * <p> Examples:
     * <pre>{@code
     * ElementHandle tweetHandle = page.querySelector(".tweet");
     * assertEquals("100", tweetHandle.evalOnSelector(".like", "node => node.innerText"));
     * assertEquals("10", tweetHandle.evalOnSelector(".retweets", "node => node.innerText"));
     * }</pre>
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evalOnSelector(selector: String, expression: String, arg: Any?): Any

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds all elements matching the specified selector in the {@code ElementHandle}'s subtree and passes an array of
     * matched elements as a first argument to {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working
     * with selectors</a> for more details.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * ElementHandle#evalOnSelectorAll ElementHandle.evalOnSelectorAll()} would wait for the promise to resolve and return its
     * value.
     *
     * <p> Examples:
     * <pre>{@code
     * ElementHandle feedHandle = page.querySelector(".feed");
     * assertEquals(Arrays.asList("Hello!", "Hi!"), feedHandle.evalOnSelectorAll(".tweet", "nodes => nodes.map(n => n.innerText)"));
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
     * Returns the return value of {@code expression}.
     *
     * <p> The method finds all elements matching the specified selector in the {@code ElementHandle}'s subtree and passes an array of
     * matched elements as a first argument to {@code expression}. See <a href="https://playwright.dev/java/docs/selectors/">Working
     * with selectors</a> for more details.
     *
     * <p> If {@code expression} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * ElementHandle#evalOnSelectorAll ElementHandle.evalOnSelectorAll()} would wait for the promise to resolve and return its
     * value.
     *
     * <p> Examples:
     * <pre>{@code
     * ElementHandle feedHandle = page.querySelector(".feed");
     * assertEquals(Arrays.asList("Hello!", "Hi!"), feedHandle.evalOnSelectorAll(".tweet", "nodes => nodes.map(n => n.innerText)"));
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
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the
     * element, fills it and triggers an {@code input} event after filling. If the element is inside the {@code <label>} element that has
     * associated <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, that control
     * will be filled instead. If the element to be filled is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element,
     * this method throws an error. Note that you can pass an empty string to clear the input field.
     *
     * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
     */
    fun fill(value: String) {
        fill(value, null)
    }

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, focuses the
     * element, fills it and triggers an {@code input} event after filling. If the element is inside the {@code <label>} element that has
     * associated <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, that control
     * will be filled instead. If the element to be filled is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element,
     * this method throws an error. Note that you can pass an empty string to clear the input field.
     *
     * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
     */
    fun fill(value: String, options: FillOptions?)

    /**
     * Calls <a href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/focus">focus</a> on the element.
     */
    fun focus()

    /**
     * Returns element attribute value.
     *
     * @param name Attribute name to get the value for.
     */
    fun getAttribute(name: String): String?

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
    fun hover() {
        hover(null)
    }

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
    fun innerHTML(): String

    /**
     * Returns the {@code element.innerText}.
     */
    fun innerText(): String

    /**
     * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
     */
    fun isChecked(): Boolean

    /**
     * Returns whether the element is disabled, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     */
    fun isDisabled(): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.
     */
    fun isEditable(): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.
     */
    fun isEnabled(): Boolean

    /**
     * Returns whether the element is hidden, the opposite of <a
     * href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.
     */
    fun isHidden(): Boolean

    /**
     * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.
     */
    fun isVisible(): Boolean

    /**
     * Returns the frame containing the given element.
     */
    fun ownerFrame(): IFrame?

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

    fun press(key: String) {
        press(key, null)
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
     *
     * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
     */
    fun press(key: String, options: PressOptions?)

    /**
     * The method finds an element matching the specified selector in the {@code ElementHandle}'s subtree. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
     * selector, returns {@code null}.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelector(selector: String): IElementHandle?

    /**
     * The method finds all elements matching the specified selector in the {@code ElementHandle}s subtree. See <a
     * href="https://playwright.dev/java/docs/selectors/">Working with selectors</a> for more details. If no elements match the
     * selector, returns empty array.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun querySelectorAll(selector: String): List<IElementHandle>?

    /**
     * Returns the buffer with the captured screenshot.
     *
     * <p> This method waits for the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then
     * scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
     */
    fun screenshot(): ByteArray {
        return screenshot(null)
    }

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
     *
     * <p> Throws when {@code elementHandle} does not point to an element <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Node/isConnected">connected</a> to a Document or a ShadowRoot.
     */
    fun scrollIntoViewIfNeeded() {
        scrollIntoViewIfNeeded(null)
    }

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then tries to
     * scroll element into view, unless it is completely visible as defined by <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Intersection_Observer_API">IntersectionObserver</a>'s {@code ratio}.
     *
     * <p> Throws when {@code elementHandle} does not point to an element <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/Node/isConnected">connected</a> to a Document or a ShadowRoot.
     */
    fun scrollIntoViewIfNeeded(options: ScrollIntoViewIfNeededOptions?)

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: String?): List<String> {
        return selectOption(value, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: String?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: IElementHandle): List<String> {
        return selectOption(values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: IElementHandle?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<String>): List<String> {
        return selectOption(values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<String>?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: SelectOption?): List<String> {
        return selectOption(values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(value: SelectOption?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<IElementHandle>): List<String> {
        return selectOption(values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<IElementHandle>?, options: SelectOptionOptions?): List<String>

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<SelectOption>): List<String> {
        return selectOption(values, null)
    }

    /**
     * Returns the array of option values that have been successfully selected.
     *
     * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If element is not a {@code <select>}
     * element, the method throws an error.
     *
     * <p> Will wait until all specified options are present in the {@code <select>} element.
     * <pre>{@code
     * // single selection matching the value
     * handle.selectOption("blue");
     * // single selection matching the label
     * handle.selectOption(new SelectOption().setLabel("Blue"));
     * // multiple selection
     * handle.selectOption(new String[] {"red", "green", "blue"});
     * }</pre>
     *
     * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
     * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
     * is considered matching if all specified properties match.
     */
    fun selectOption(values: Array<SelectOption>?, options: SelectOptionOptions?): List<String>

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then focuses
     * the element and selects all its text content.
     */
    fun selectText() {
        selectText(null)
    }

    /**
     * This method waits for <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks, then focuses
     * the element and selects all its text content.
     */
    fun selectText(options: SelectTextOptions?)

    /**
     * This method expects {@code elementHandle} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Path) {
        setInputFiles(files, null)
    }

    /**
     * This method expects {@code elementHandle} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Path, options: SetInputFilesOptions?)

    /**
     * This method expects {@code elementHandle} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Array<Path>) {
        setInputFiles(files, null)
    }

    /**
     * This method expects {@code elementHandle} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Array<Path>, options: SetInputFilesOptions?)

    /**
     * This method expects {@code elementHandle} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: FilePayload) {
        setInputFiles(files, null)
    }

    /**
     * This method expects {@code elementHandle} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: FilePayload, options: SetInputFilesOptions?)

    /**
     * This method expects {@code elementHandle} to point to an <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>.
     *
     * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
     * are resolved relative to the the current working directory. For empty array, clears the selected files.
     */
    fun setInputFiles(files: Array<FilePayload>) {
        setInputFiles(files, null)
    }

    /**
     * This method expects {@code elementHandle} to point to an <a
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
     * <p> <strong>NOTE:</strong> {@code elementHandle.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
     */
    fun tap() {
        tap(null)
    }

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
     * <p> <strong>NOTE:</strong> {@code elementHandle.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
     */
    fun tap(options: TapOptions?)

    /**
     * Returns the {@code node.textContent}.
     */
    fun textContent(): String?

    /**
     * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link ElementHandle#press ElementHandle.press()}.
     * <pre>{@code
     * elementHandle.type("Hello"); // Types instantly
     * elementHandle.type("World", new ElementHandle.TypeOptions().setDelay(100)); // Types slower, like a user
     * }</pre>
     *
     * <p> An example of typing into a text field and then submitting the form:
     * <pre>{@code
     * ElementHandle elementHandle = page.querySelector("input");
     * elementHandle.type("some text");
     * elementHandle.press("Enter");
     * }</pre>
     *
     * @param text A text to type into a focused element.
     */
    fun type(text: String) {
        type(text, null)
    }

    /**
     * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
     *
     * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link ElementHandle#press ElementHandle.press()}.
     * <pre>{@code
     * elementHandle.type("Hello"); // Types instantly
     * elementHandle.type("World", new ElementHandle.TypeOptions().setDelay(100)); // Types slower, like a user
     * }</pre>
     *
     * <p> An example of typing into a text field and then submitting the form:
     * <pre>{@code
     * ElementHandle elementHandle = page.querySelector("input");
     * elementHandle.type("some text");
     * elementHandle.press("Enter");
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
    fun uncheck() {
        uncheck(null)
    }

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

    /**
     * Returns when the element satisfies the {@code state}.
     *
     * <p> Depending on the {@code state} parameter, this method waits for one of the <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks to pass. This method throws when the
     * element is detached while waiting, unless waiting for the {@code "hidden"} state.
     * <ul>
     * <li> {@code "visible"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.</li>
     * <li> {@code "hidden"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#visible">not visible</a>
     * or <a href="https://playwright.dev/java/docs/actionability/#attached">not attached</a>. Note that waiting for hidden
     * does not throw when the element detaches.</li>
     * <li> {@code "stable"} Wait until the element is both <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>
     * and <a href="https://playwright.dev/java/docs/actionability/#stable">stable</a>.</li>
     * <li> {@code "enabled"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.</li>
     * <li> {@code "disabled"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">not
     * enabled</a>.</li>
     * <li> {@code "editable"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.</li>
     * </ul>
     *
     * <p> If the element does not satisfy the condition for the {@code timeout} milliseconds, this method will throw.
     *
     * @param state A state to wait for, see below for more details.
     */
    fun waitForElementState(state: ElementState) {
        waitForElementState(state, null)
    }

    /**
     * Returns when the element satisfies the {@code state}.
     *
     * <p> Depending on the {@code state} parameter, this method waits for one of the <a
     * href="https://playwright.dev/java/docs/actionability/">actionability</a> checks to pass. This method throws when the
     * element is detached while waiting, unless waiting for the {@code "hidden"} state.
     * <ul>
     * <li> {@code "visible"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>.</li>
     * <li> {@code "hidden"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#visible">not visible</a>
     * or <a href="https://playwright.dev/java/docs/actionability/#attached">not attached</a>. Note that waiting for hidden
     * does not throw when the element detaches.</li>
     * <li> {@code "stable"} Wait until the element is both <a href="https://playwright.dev/java/docs/actionability/#visible">visible</a>
     * and <a href="https://playwright.dev/java/docs/actionability/#stable">stable</a>.</li>
     * <li> {@code "enabled"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">enabled</a>.</li>
     * <li> {@code "disabled"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#enabled">not
     * enabled</a>.</li>
     * <li> {@code "editable"} Wait until the element is <a href="https://playwright.dev/java/docs/actionability/#editable">editable</a>.</li>
     * </ul>
     *
     * <p> If the element does not satisfy the condition for the {@code timeout} milliseconds, this method will throw.
     *
     * @param state A state to wait for, see below for more details.
     */
    fun waitForElementState(state: ElementState?, options: WaitForElementStateOptions?)

    /**
     * Returns element specified by selector when it satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
     * {@code detached}.
     *
     * <p> Wait for the {@code selector} relative to the element handle to satisfy {@code state} option (either appear/disappear from dom, or
     * become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method
     * will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will
     * throw.
     * <pre>{@code
     * page.setContent("<div><span></span></div>");
     * ElementHandle div = page.querySelector("div");
     * // Waiting for the "span" selector relative to the div.
     * ElementHandle span = div.waitForSelector("span", new ElementHandle.WaitForSelectorOptions()
     *   .setState(WaitForSelectorState.ATTACHED));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> This method does not work across navigations, use {@link Page#waitForSelector Page.waitForSelector()} instead.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun waitForSelector(selector: String): IElementHandle? {
        return waitForSelector(selector, null)
    }

    /**
     * Returns element specified by selector when it satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
     * {@code detached}.
     *
     * <p> Wait for the {@code selector} relative to the element handle to satisfy {@code state} option (either appear/disappear from dom, or
     * become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method
     * will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will
     * throw.
     * <pre>{@code
     * page.setContent("<div><span></span></div>");
     * ElementHandle div = page.querySelector("div");
     * // Waiting for the "span" selector relative to the div.
     * ElementHandle span = div.waitForSelector("span", new ElementHandle.WaitForSelectorOptions()
     *   .setState(WaitForSelectorState.ATTACHED));
     * }</pre>
     *
     * <p> <strong>NOTE:</strong> This method does not work across navigations, use {@link Page#waitForSelector Page.waitForSelector()} instead.
     *
     * @param selector A selector to query for. See <a href="https://playwright.dev/java/docs/selectors/">working with selectors</a> for more
     * details.
     */
    fun waitForSelector(selector: String, options: WaitForElementStateOptions?): IElementHandle?
}