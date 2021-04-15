package com.playwright.remote.domain.serialize

data class SerializedError(
    val error: Error?,
    val value: SerializedValue
) {
    data class Error(
        val message: String,
        val name: String,
        val stack: String
    ) {
        override fun toString(): String {
            return "Error(\nmessage=$message, \nname=$name, \nstack=$stack\n)"
        }
    }

    class SerializedValue(
        var n: Number? = null,
        var b: Boolean? = null,
        var s: String? = null,
        var v: String? = null,
        var d: String? = null,
        var r: R? = null,
        var a: Array<SerializedValue>? = null,
        var o: Array<O>? = null,
        var h: Number? = null
    ) {
        data class R(
            val p: String,
            val f: String
        )

        data class O(
            var k: String? = null,
            var v: SerializedValue? = null
        )
    }
}
