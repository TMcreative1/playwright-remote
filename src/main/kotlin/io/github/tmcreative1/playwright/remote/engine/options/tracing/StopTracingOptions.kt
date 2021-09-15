package io.github.tmcreative1.playwright.remote.engine.options.tracing

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class StopTracingOptions @JvmOverloads constructor(
    /**
     * Export trace into the file with the given name.
     */
    var path: Path? = null,
    @Transient private val builder: IBuilder<StopTracingOptions>
) {
    init {
        builder.build(this)
    }
}