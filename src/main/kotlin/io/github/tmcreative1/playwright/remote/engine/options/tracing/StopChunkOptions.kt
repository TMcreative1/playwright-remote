package io.github.tmcreative1.playwright.remote.engine.options.tracing

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class StopChunkOptions @JvmOverloads constructor(
    /**
     * Export trace collected since the last {@link Tracing#startChunk Tracing.startChunk()} call into the file with the given
     * path.
     */
    var path: Path? = null,
    @Transient private val builder: IBuilder<StopChunkOptions>
) {
    init {
        builder.build(this)
    }
}
