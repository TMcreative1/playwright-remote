package io.github.tmcreative1.playwright.remote.engine.options.element

import io.github.tmcreative1.playwright.remote.engine.options.Position
import io.github.tmcreative1.playwright.remote.core.enums.KeyboardModifier
import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class HoverOptions @JvmOverloads constructor(
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
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    var position: Position? = null,
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link Page#setDefaultTimeout
     * Page.setDefaultTimeout()} methods.
     */
    var timeout: Double? = null,
    @Transient private val builder: IBuilder<HoverOptions>
) {
    init {
        builder.build(this)
    }
}
