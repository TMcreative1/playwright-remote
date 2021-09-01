package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class ViewportSize @JvmOverloads constructor(
    /**
     * page width in pixels.
     */
    var width: Int? = null,
    /**
     * page height in pixels.
     */
    var height: Int? = null,
    @Transient private val builder: IBuilder<ViewportSize>,
) {
    init {
        builder.build(this)
    }

    override fun equals(other: Any?): Boolean = (other is ViewportSize)
            && width == other.width
            && height == other.height

    override fun hashCode(): Int {
        var result = if (width != null) width.hashCode() else 0
        result = 31 * result + if (height != null) height.hashCode() else 0
        return result
    }
}