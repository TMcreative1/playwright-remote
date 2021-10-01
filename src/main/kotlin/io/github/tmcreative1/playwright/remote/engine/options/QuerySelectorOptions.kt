package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class QuerySelectorOptions @JvmOverloads constructor(
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more then one
     * element, the call throws an exception.
     */
    var strict: Boolean? = null,
    @Transient private val builder: IBuilder<QuerySelectorOptions>
) {
    init {
        builder.build(this)
    }
}
