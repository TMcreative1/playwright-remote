package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class Geolocation @JvmOverloads constructor(
    var latitude: Double? = null,
    var longitude: Double? = null,
    var accuracy: Double? = null,
    @Transient private val builder: IBuilder<Geolocation>
) {

    init {
        builder.build(this)
    }
}