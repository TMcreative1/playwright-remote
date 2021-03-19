package com.playwright.remote.engine.options

class Geolocation(
    val latitude: Double,
    val longitude: Double,
    var accuracy: Double? = null,
    fn: Geolocation.() -> Unit
) {

    init {
        fn()
    }
}