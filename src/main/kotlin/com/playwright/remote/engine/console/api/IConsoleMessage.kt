package com.playwright.remote.engine.console.api

import com.playwright.remote.engine.handle.js.api.IJSHandle

/**
 * {@code ConsoleMessage} objects are dispatched by page via the {@link Page#onConsole Page.onConsole()} event.
 */
interface IConsoleMessage {
    fun args(): List<IJSHandle>

    /**
     * URL of the resource followed by 0-based line and column numbers in the resource formatted as {@code URL:line:column}.
     */
    fun location(): String
    fun text(): String

    /**
     * One of the following values: {@code "log"}, {@code "debug"}, {@code "info"}, {@code "error"}, {@code "warning"}, {@code "dir"}, {@code "dirxml"}, {@code "table"},
     * {@code "trace"}, {@code "clear"}, {@code "startGroup"}, {@code "startGroupCollapsed"}, {@code "endGroup"}, {@code "assert"}, {@code "profile"}, {@code "profileEnd"},
     * {@code "count"}, {@code "timeEnd"}.
     */
    fun type(): String
}