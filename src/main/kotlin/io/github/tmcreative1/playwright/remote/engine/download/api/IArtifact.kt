package io.github.tmcreative1.playwright.remote.engine.download.api

import java.io.InputStream
import java.nio.file.Path

interface IArtifact {
    /**
     * Returns readable stream for current download or {@code null} if download failed.
     */
    fun createReadStream(): InputStream?

    /**
     * Deletes the downloaded file.
     */
    fun delete()

    /**
     * Returns download error if any.
     */
    fun failure(): String?

    /**
     * Saves the download to a user-specified path.
     *
     * @param path Path where the download should be saved.
     */
    fun saveAs(path: Path)
}