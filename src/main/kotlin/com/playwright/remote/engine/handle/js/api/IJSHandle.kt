package com.playwright.remote.engine.handle.js.api

import com.playwright.remote.engine.handle.element.api.IElementHandle

/**
 * JSHandle represents an in-page JavaScript object. JSHandles can be created with the {@link Page#evaluateHandle
 * Page.evaluateHandle()} method.
 * <pre>{@code
 * JSHandle windowHandle = page.evaluateHandle("() => window");
 * // ...
 * }</pre>
 *
 * <p> JSHandle prevents the referenced JavaScript object being garbage collected unless the handle is exposed with {@link
 * JSHandle#dispose JSHandle.dispose()}. JSHandles are auto-disposed when their origin frame gets navigated or the parent
 * context gets destroyed.
 *
 * <p> JSHandle instances can be used as an argument in {@link Page#evalOnSelector Page.evalOnSelector()}, {@link Page#evaluate
 * Page.evaluate()} and {@link Page#evaluateHandle Page.evaluateHandle()} methods.
 */
interface IJSHandle {
    /**
     * Returns either {@code null} or the object handle itself, if the object handle is an instance of {@code ElementHandle}.
     */
    fun asElement(): IElementHandle?

    /**
     * The {@code jsHandle.dispose} method stops referencing the element handle.
     */
    fun dispose()

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
     * ElementHandle tweetHandle = page.querySelector(".tweet .retweets");
     * assertEquals("10 retweets", tweetHandle.evaluate("node => node.innerText"));
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
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
     * ElementHandle tweetHandle = page.querySelector(".tweet .retweets");
     * assertEquals("10 retweets", tweetHandle.evaluate("node => node.innerText"));
     * }</pre>
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluate(expression: String, arg: Any?): Any

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> This method passes this handle as the first argument to {@code expression}.
     *
     * <p> The only difference between {@code jsHandle.evaluate} and {@code jsHandle.evaluateHandle} is that {@code jsHandle.evaluateHandle} returns
     * {@code JSHandle}.
     *
     * <p> If the function passed to the {@code jsHandle.evaluateHandle} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then
     * {@code jsHandle.evaluateHandle} would wait for the promise to resolve and return its value.
     *
     * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluateHandle(expression: String): IJSHandle = evaluateHandle(expression, null)

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> This method passes this handle as the first argument to {@code expression}.
     *
     * <p> The only difference between {@code jsHandle.evaluate} and {@code jsHandle.evaluateHandle} is that {@code jsHandle.evaluateHandle} returns
     * {@code JSHandle}.
     *
     * <p> If the function passed to the {@code jsHandle.evaluateHandle} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then
     * {@code jsHandle.evaluateHandle} would wait for the promise to resolve and return its value.
     *
     * <p> See {@link Page#evaluateHandle Page.evaluateHandle()} for more details.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluateHandle(expression: String, arg: Any?): IJSHandle

    /**
     * The method returns a map with **own property names** as keys and JSHandle instances for the property values.
     * <pre>{@code
     * JSHandle handle = page.evaluateHandle("() => ({window, document}"););
     * Map<String, JSHandle> properties = handle.getProperties();
     * JSHandle windowHandle = properties.get("window");
     * JSHandle documentHandle = properties.get("document");
     * handle.dispose();
     * }</pre>
     */
    fun getProperties(): Map<String, IJSHandle>

    /**
     * Fetches a single property from the referenced object.
     *
     * @param propertyName property to get
     */
    fun getProperty(propertyName: String): IJSHandle

    /**
     * Returns a JSON representation of the object. If the object has a {@code toJSON} function, it **will not be called**.
     *
     * <p> <strong>NOTE:</strong> The method will return an empty JSON object if the referenced object is not stringifiable. It will throw an error if the
     * object has circular references.
     */
    fun jsonValue(): Any
}