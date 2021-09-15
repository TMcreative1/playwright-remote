package io.github.tmcreative1.playwright.remote.engine.download.api

import io.github.tmcreative1.playwright.remote.engine.page.api.IPage

interface IDownload : IArtifact {

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

    /**
     * Get the page that the download belongs to.
     */
    fun page(): IPage
}