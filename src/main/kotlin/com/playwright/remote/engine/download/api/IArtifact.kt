package com.playwright.remote.engine.download.api

import java.io.InputStream
import java.nio.file.Path

interface IArtifact {
    /**
     * Cancels a download. Will not fail if the download is already finished or canceled. Upon successful cancellations,
     * {@code download.failure()} would resolve to {@code "canceled"}.
     */
    fun cancel()

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