package com.playwright.remote.engine.options

import com.playwright.remote.core.enums.Media
import com.playwright.remote.engine.options.api.IBuilder
import com.playwright.remote.engine.options.enum.ColorScheme

data class EmulateMediaOptions @JvmOverloads constructor(
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code "no-preference"}. Passing
     * {@code null} disables color scheme emulation.
     */
    var colorScheme: ColorScheme? = null,
    /**
     * Changes the CSS media type of the page. The only allowed values are {@code "screen"}, {@code "print"} and {@code null}. Passing {@code null}
     * disables CSS media emulation.
     */
    var media: Media? = null,
    @Transient private val builder: IBuilder<EmulateMediaOptions>
) {
    init {
        builder.build(this)
    }
}
