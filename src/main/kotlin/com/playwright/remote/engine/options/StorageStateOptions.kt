package com.playwright.remote.engine.options

import java.nio.file.Path

class StorageStateOptions(
    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    var path: Path? = null,
    fn: StorageStateOptions.() -> Unit,
) {
    init {
        fn()
    }
}