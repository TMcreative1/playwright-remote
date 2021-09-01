package io.github.tmcreative1.playwright.remote.engine.touchscreen.api

/**
 * The Touchscreen class operates in main-frame CSS pixels relative to the top-left corner of the viewport. Methods on the
 * touchscreen can only be used in browser contexts that have been intialized with {@code hasTouch} set to true.
 */
interface ITouchScreen {
    /**
     * Dispatches a {@code touchstart} and {@code touchend} event with a single touch at the position ({@code x},{@code y}).
     */
    fun tap(x: Double, y: Double)
}