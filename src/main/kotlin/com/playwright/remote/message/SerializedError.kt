package com.playwright.remote.message

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
        val n: Number,
        val b: Boolean,
        val s: String,
        val v: String,
        val d: String,
        val r: R,
        val a: Array<SerializedValue>,
        val o: Array<O>,
        val h: Number
    ) {
        data class R(
            val p: String,
            val f: String
        )

        data class O(
            val k: String,
            val v: SerializedValue
        )
    }
}
