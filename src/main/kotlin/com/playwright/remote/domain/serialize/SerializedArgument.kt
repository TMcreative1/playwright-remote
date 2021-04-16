package com.playwright.remote.domain.serialize

import com.playwright.remote.domain.serialize.SerializedError.SerializedValue
import com.playwright.remote.engine.options.api.IBuilder

data class SerializedArgument @JvmOverloads constructor(
    var value: SerializedValue? = null,
    var handles: Array<Channel>? = null,
    private val builder: IBuilder<SerializedArgument>
) {
    data class Channel(val guid: String)

    init {
        builder.build(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SerializedArgument

        if (value != other.value) return false
        if (handles != null) {
            if (other.handles == null) return false
            if (!handles.contentEquals(other.handles)) return false
        } else if (other.handles != null) return false
        if (builder != other.builder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + (handles?.contentHashCode() ?: 0)
        result = 31 * result + builder.hashCode()
        return result
    }
}