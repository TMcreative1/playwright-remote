package io.github.tmcreative1.playwright.remote.engine.options

import com.google.gson.annotations.SerializedName
import io.github.tmcreative1.playwright.remote.core.enums.Media
import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import io.github.tmcreative1.playwright.remote.engine.options.enum.ColorScheme
import java.util.*

data class EmulateMediaOptions @JvmOverloads constructor(
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code "no-preference"}. Passing
     * {@code null} disables color scheme emulation.
     */
    @SerializedName("colorScheme")
    private var _colorScheme: Optional<ColorScheme>? = null,

    /**
     * Changes the CSS media type of the page. The only allowed values are {@code "screen"}, {@code "print"} and {@code null}. Passing {@code null}
     * disables CSS media emulation.
     */
    @SerializedName("media")
    private var _media: Optional<Media>? = null,
    @Transient private val builder: IBuilder<EmulateMediaOptions>
) {
    @Transient
    var colorScheme: ColorScheme? = null
        set(value) {
            _colorScheme = Optional.ofNullable(value)
        }

    @Transient
    var media: Media? = null
        set(value) {
            _media = Optional.ofNullable(value)
        }

    init {
        builder.build(this)
    }
}
