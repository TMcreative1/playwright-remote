package io.github.tmcreative1.playwright.remote.engine.options.element

import io.github.tmcreative1.playwright.remote.core.enums.KeyboardModifier
import io.github.tmcreative1.playwright.remote.engine.options.Position
import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder
import io.github.tmcreative1.playwright.remote.engine.options.enum.MouseButton

data class DoubleClickOptions @JvmOverloads constructor(
    /**
     * Defaults to {@code left}.
     */
    var button: MouseButton? = null,
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    var delay: Double? = null,
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability/">actionability</a> checks. Defaults to
     * {@code false}.
     */
    var force: Boolean? = null,
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    var modifiers: List<KeyboardModifier>? = null,
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    var noWaitAfter: Boolean? = null,
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    var position: Position? = null,
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more then one
     * element, the call throws an exception.
     */
    var strict: Boolean? = null,
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
    @Transient private val builder: IBuilder<DoubleClickOptions>
) {
    init {
        builder.build(this)
    }
}
