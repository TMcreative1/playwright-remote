package io.github.tmcreative1.playwright.remote.engine.options

import io.github.tmcreative1.playwright.remote.engine.options.api.IBuilder

data class SelectOption @JvmOverloads constructor(
    /**
     * Matches by {@code option.value}. Optional.
     */
    var value: String? = null,
    /**
     * Matches by {@code option.label}. Optional.
     */
    var label: String? = null,
    /**
     * Matches by the index. Optional.
     */
    var index: Int? = null,
    @Transient private val builder: IBuilder<SelectOption>
) {
    init {
        builder.build(this)
    }
}