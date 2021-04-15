package com.playwright.remote.domain.serialize

data class SerializedError(
    val error: Error?,
    val value: SerializedValue,
) {
    data class Error(
        val message: String,
        val name: String,
        val stack: String,
    ) {
        override fun toString(): String {
            return "Error(\nmessage=$message, \nname=$name, \nstack=$stack\n)"
        }
    }

    data class SerializedValue(
        var n: Number? = null,
        var b: Boolean? = null,
        var s: String? = null,
        var v: String? = null,
        var d: String? = null,
        var r: R? = null,
        var a: Array<SerializedValue>? = null,
        var o: Array<O>? = null,
        var h: Number? = null,
    ) {
        data class R(
            val p: String,
            val f: String,
        )

        data class O(
            var k: String? = null,
            var v: SerializedValue? = null,
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SerializedValue

            if (n != other.n) return false
            if (b != other.b) return false
            if (s != other.s) return false
            if (v != other.v) return false
            if (d != other.d) return false
            if (r != other.r) return false
            if (a != null) {
                if (other.a == null) return false
                if (!a.contentEquals(other.a)) return false
            } else if (other.a != null) return false
            if (o != null) {
                if (other.o == null) return false
                if (!o.contentEquals(other.o)) return false
            } else if (other.o != null) return false
            if (h != other.h) return false

            return true
        }

        override fun hashCode(): Int {
            var result = n?.hashCode() ?: 0
            result = 31 * result + (b?.hashCode() ?: 0)
            result = 31 * result + (s?.hashCode() ?: 0)
            result = 31 * result + (v?.hashCode() ?: 0)
            result = 31 * result + (d?.hashCode() ?: 0)
            result = 31 * result + (r?.hashCode() ?: 0)
            result = 31 * result + (a?.contentHashCode() ?: 0)
            result = 31 * result + (o?.contentHashCode() ?: 0)
            result = 31 * result + (h?.hashCode() ?: 0)
            return result
        }
    }
}
