package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class RecordVideoSize @JvmOverloads constructor(
    var width: Int? = null,
    var height: Int? = null,
    @Transient private val builder: IBuilder<RecordVideoSize>
) {
    init {
        builder.build(this)
    }
}