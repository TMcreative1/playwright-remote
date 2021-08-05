package com.playwright.remote.engine.download.api

import java.io.InputStream
import java.nio.file.Path

interface IDownload {

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

    /**
     * Returns suggested filename for this download. It is typically computed by the browser from the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Disposition">{@code Content-Disposition}</a> response
     * header or the {@code download} attribute. See the spec on <a
     * href="https://html.spec.whatwg.org/#downloading-resources">whatwg</a>. Different browsers can use different logic for
     * computing it.
     */
    fun suggestedFilename(): String

    /**
     * Returns downloaded url.
     */
    fun url(): String
}