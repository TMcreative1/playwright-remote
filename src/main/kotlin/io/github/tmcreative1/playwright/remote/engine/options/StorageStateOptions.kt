package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class StorageStateOptions @JvmOverloads constructor(
    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    var path: Path? = null,
    @Transient private val builder: IBuilder<StorageStateOptions>
) {
    init {
        builder.build(this)
    }
}