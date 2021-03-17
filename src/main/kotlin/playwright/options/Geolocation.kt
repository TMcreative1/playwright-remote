package playwright.options

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