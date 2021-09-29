package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class StartTracingOptions @JvmOverloads constructor(
    /**
     * specify custom categories to use instead of default.
     */
    var categories: List<String>? = null,

    /**
     * A path to write the trace file to.
     */
    var path: Path? = null,

    /**
     * captures screenshots in the trace.
     */
    var screenshots: Boolean? = null,

    @Transient private val builder: IBuilder<StartTracingOptions>
) {
    init {
        builder.build(this)
    }
}