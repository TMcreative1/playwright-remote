package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class AddScriptTagOptions @JvmOverloads constructor(
    /**
     * Raw JavaScript content to be injected into frame.
     */
    var content: String? = null,
    /**
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the
     * current working directory.
     */
    var path: Path? = null,
    /**
     * Script type. Use 'module' in order to load a Javascript ES6 module. See <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script">script</a> for more details.
     */
    var type: String? = null,
    /**
     * URL of a script to be added.
     */
    var url: String? = null,
    @Transient private val builder: IBuilder<AddScriptTagOptions>
) {
    init {
        builder.build(this)
    }
}
