package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder

data class Geolocation @JvmOverloads constructor(
    val latitude: Double,
    val longitude: Double,
    var accuracy: Double? = null,
    private val builder: IBuilder<Geolocation>
) {

    init {
        builder.build(this)
    }
}