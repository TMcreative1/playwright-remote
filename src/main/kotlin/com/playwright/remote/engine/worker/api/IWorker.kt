package com.playwright.remote.engine.worker.api

import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.options.WaitForCloseOptions

interface IWorker {
    /**
     * Emitted when this dedicated <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">WebWorker</a> is
     * terminated.
     */
    fun onClose(handler: (IWorker) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onClose onClose(handler)}.
     */
    fun offClose(handler: (IWorker) -> Unit)

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> If the function passed to the {@link Worker#evaluate Worker.evaluate()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Worker#evaluate Worker.evaluate()} would wait for the promise to resolve and return its value.
     *
     * <p> If the function passed to the {@link Worker#evaluate Worker.evaluate()} returns a non-[Serializable] value, then {@link
     * Worker#evaluate Worker.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional values
     * that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluate(expression: String): Any {
        return evaluate(expression, null)
    }

    /**
     * Returns the return value of {@code expression}.
     *
     * <p> If the function passed to the {@link Worker#evaluate Worker.evaluate()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Worker#evaluate Worker.evaluate()} would wait for the promise to resolve and return its value.
     *
     * <p> If the function passed to the {@link Worker#evaluate Worker.evaluate()} returns a non-[Serializable] value, then {@link
     * Worker#evaluate Worker.evaluate()} returns {@code undefined}. Playwright also supports transferring some additional values
     * that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluate(expression: String, arg: Any?): Any

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> The only difference between {@link Worker#evaluate Worker.evaluate()} and {@link Worker#evaluateHandle
     * Worker.evaluateHandle()} is that {@link Worker#evaluateHandle Worker.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function passed to the {@link Worker#evaluateHandle Worker.evaluateHandle()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Worker#evaluateHandle Worker.evaluateHandle()} would wait for the promise to resolve and return its value.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     */
    fun evaluateHandle(expression: String): IJSHandle {
        return evaluateHandle(expression, null)
    }

    /**
     * Returns the return value of {@code expression} as a {@code JSHandle}.
     *
     * <p> The only difference between {@link Worker#evaluate Worker.evaluate()} and {@link Worker#evaluateHandle
     * Worker.evaluateHandle()} is that {@link Worker#evaluateHandle Worker.evaluateHandle()} returns {@code JSHandle}.
     *
     * <p> If the function passed to the {@link Worker#evaluateHandle Worker.evaluateHandle()} returns a <a
     * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
     * Worker#evaluateHandle Worker.evaluateHandle()} would wait for the promise to resolve and return its value.
     *
     * @param expression JavaScript expression to be evaluated in the browser context. If it looks like a function declaration, it is interpreted
     * as a function. Otherwise, evaluated as an expression.
     * @param arg Optional argument to pass to {@code expression}.
     */
    fun evaluateHandle(expression: String, arg: Any?): IJSHandle

    fun url(): String

    /**
     * Performs action and waits for the Worker to close.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForClose(callback: () -> Unit): IWorker {
        return waitForClose(null, callback)
    }

    /**
     * Performs action and waits for the Worker to close.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForClose(options: WaitForCloseOptions?, callback: () -> Unit): IWorker
}