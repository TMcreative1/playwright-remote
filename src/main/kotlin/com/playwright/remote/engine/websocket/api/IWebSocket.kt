package com.playwright.remote.engine.websocket.api

import com.playwright.remote.engine.options.wait.WaitForFrameReceivedOptions
import com.playwright.remote.engine.options.wait.WaitForFrameSentOptions

interface IWebSocket {
    /**
     * Fired when the websocket closes.
     */
    fun onClose(handler: (IWebSocket) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onClose onClose(handler)}.
     */
    fun offClose(handler: (IWebSocket) -> Unit)

    /**
     * Fired when the websocket receives a frame.
     */
    fun onFrameReceived(handler: (IWebSocketFrame) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onFrameReceived onFrameReceived(handler)}.
     */
    fun offFrameReceived(handler: (IWebSocketFrame) -> Unit)

    /**
     * Fired when the websocket sends a frame.
     */
    fun onFrameSent(handler: (IWebSocketFrame) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onFrameSent onFrameSent(handler)}.
     */
    fun offFrameSent(handler: (IWebSocketFrame) -> Unit)

    /**
     * Fired when the websocket has an error.
     */
    fun onSocketError(handler: (String) -> Unit)

    /**
     * Removes handler that was previously added with {@link #onSocketError onSocketError(handler)}.
     */
    fun offSocketError(handler: (String) -> Unit)

    /**
     * Indicates that the web socket has been closed.
     */
    fun isClosed(): Boolean

    /**
     * Contains the URL of the WebSocket.
     */
    fun url(): String

    /**
     * Performs action and waits for a frame to be sent. If predicate is provided, it passes {@code WebSocketFrame} value into the
     * {@code predicate} function and waits for {@code predicate(webSocketFrame)} to return a truthy value. Will throw an error if the
     * WebSocket or Page is closed before the frame is received.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForFrameReceived(callback: () -> Unit): IWebSocketFrame {
        return waitForFrameReceived(null, callback)
    }

    /**
     * Performs action and waits for a frame to be sent. If predicate is provided, it passes {@code WebSocketFrame} value into the
     * {@code predicate} function and waits for {@code predicate(webSocketFrame)} to return a truthy value. Will throw an error if the
     * WebSocket or Page is closed before the frame is received.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForFrameReceived(options: WaitForFrameReceivedOptions?, callback: () -> Unit): IWebSocketFrame

    /**
     * Performs action and waits for a frame to be sent. If predicate is provided, it passes {@code WebSocketFrame} value into the
     * {@code predicate} function and waits for {@code predicate(webSocketFrame)} to return a truthy value. Will throw an error if the
     * WebSocket or Page is closed before the frame is sent.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForFrameSent(callback: () -> Unit): IWebSocketFrame {
        return waitForFrameSent(null, callback)
    }

    /**
     * Performs action and waits for a frame to be sent. If predicate is provided, it passes {@code WebSocketFrame} value into the
     * {@code predicate} function and waits for {@code predicate(webSocketFrame)} to return a truthy value. Will throw an error if the
     * WebSocket or Page is closed before the frame is sent.
     *
     * @param callback Callback that performs the action triggering the event.
     */
    fun waitForFrameSent(options: WaitForFrameSentOptions?, callback: () -> Unit): IWebSocketFrame
}