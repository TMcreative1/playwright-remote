package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

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
    private val builder: IBuilder<SelectOption>
) {
    init {
        builder.build(this)
    }
}