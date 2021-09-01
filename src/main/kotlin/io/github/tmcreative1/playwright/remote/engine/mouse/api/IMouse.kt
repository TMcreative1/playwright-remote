package com.playwright.remote.engine.mouse.api

import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.mouse.ClickOptions
import com.playwright.remote.engine.options.mouse.DoubleClickOptions

/**
 * The Mouse class operates in main-frame CSS pixels relative to the top-left corner of the viewport.
 *
 * <p> Every {@code page} object has its own Mouse, accessible with {@link Page#mouse Page.mouse()}.
 * <pre>{@code
 * // Using ‘page.mouse’ to trace a 100x100 square.
 * page.mouse().move(0, 0);
 * page.mouse().down();
 * page.mouse().move(0, 100);
 * page.mouse().move(100, 100);
 * page.mouse().move(100, 0);
 * page.mouse().move(0, 0);
 * page.mouse().up();
 * }</pre>
 */
interface IMouse {
    /**
     * Shortcut for {@link Mouse#move Mouse.move()}, {@link Mouse#down Mouse.down()}, {@link Mouse#up Mouse.up()}.
     */
    fun click(x: Double, y: Double) = click(x, y, ClickOptions {})

    /**
     * Shortcut for {@link Mouse#move Mouse.move()}, {@link Mouse#down Mouse.down()}, {@link Mouse#up Mouse.up()}.
     */
    fun click(x: Double, y: Double, options: ClickOptions)

    /**
     * Shortcut for {@link Mouse#move Mouse.move()}, {@link Mouse#down Mouse.down()}, {@link Mouse#up Mouse.up()}, {@link
     * Mouse#down Mouse.down()} and {@link Mouse#up Mouse.up()}.
     */
    fun doubleClick(x: Double, y: Double) = doubleClick(x, y, null)

    /**
     * Shortcut for {@link Mouse#move Mouse.move()}, {@link Mouse#down Mouse.down()}, {@link Mouse#up Mouse.up()}, {@link
     * Mouse#down Mouse.down()} and {@link Mouse#up Mouse.up()}.
     */
    fun doubleClick(x: Double, y: Double, options: DoubleClickOptions?)

    /**
     * Dispatches a {@code mousedown} event.
     */
    fun down() = down(DownOptions {})

    /**
     * Dispatches a {@code mousedown} event.
     */
    fun down(options: DownOptions)

    /**
     * Dispatches a {@code mousemove} event.
     */
    fun move(x: Double, y: Double) = move(x, y, MoveOptions {})

    /**
     * Dispatches a {@code mousemove} event.
     */
    fun move(x: Double, y: Double, options: MoveOptions)

    /**
     * Dispatches a {@code mouseup} event.
     */
    fun up() = up(UpOptions {})

    /**
     * Dispatches a {@code mouseup} event.
     */
    fun up(options: UpOptions)
}