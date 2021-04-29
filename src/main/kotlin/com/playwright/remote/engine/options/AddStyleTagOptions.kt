package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class AddStyleTagOptions @JvmOverloads constructor(
    /**
     * Raw CSS content to be injected into frame.
     */
    var content: String? = null,
    /**
     * Path to the CSS file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the
     * current working directory.
     */
    var path: Path? = null,
    /**
     * URL of the {@code <link>} tag.
     */
    var url: String? = null,
    @Transient private val builder: IBuilder<AddStyleTagOptions>
) {
    init {
        builder.build(this)
    }
}
