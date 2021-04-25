package com.playwright.remote.engine.options.element

import com.playwright.remote.core.enums.ScreenshotType
import com.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class ScreenshotOptions @JvmOverloads constructor(
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg} images.
     * Defaults to {@code false}.
     */
    var omitBackground: Boolean? = null,
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a relative
     * path, then it is resolved relative to the current working directory. If no path is provided, the image won't be saved to
     * the disk.
     */
    var path: Path? = null,
    /**
     * The quality of the image, between 0-100. Not applicable to {@code png} images.
     */
    var quality: Int? = null,
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    var type: ScreenshotType? = null,
    @Transient private val builder: IBuilder<ScreenshotOptions>
) {
    init {
        builder.build(this)
    }
}