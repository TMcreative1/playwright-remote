package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class DragAndDropOptions @JvmOverloads constructor(
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
     * {@code false}.
     */
    var force: Boolean? = null,
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    var noWaitAfter: Boolean? = null,
    /**
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    var sourcePosition: Position? = null,
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more then one
     * element, the call throws an exception.
     */
    var strict: Boolean? = null,
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    var targetPosition: Position? = null,
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability/">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    var trial: Boolean? = null,
    @Transient private val builder: IBuilder<DragAndDropOptions>
) {
    init {
        builder.build(this)
    }
}
