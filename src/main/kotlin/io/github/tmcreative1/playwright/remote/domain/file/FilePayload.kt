package com.playwright.remote.domain.file

import com.playwright.remote.engine.options.api.IBuilder

data class FilePayload @JvmOverloads constructor(
    /**
     * File name
     */
    var name: String? = null,
    /**
     * File type
     */
    var mimeType: String? = null,
    /**
     * File content
     */
    var buffer: ByteArray? = null,
    @Transient private val builder: IBuilder<FilePayload>
) {
    init {
        builder.build(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FilePayload

        if (name != other.name) return false
        if (mimeType != other.mimeType) return false
        if (buffer != null) {
            if (other.buffer == null) return false
            if (!buffer.contentEquals(other.buffer)) return false
        } else if (other.buffer != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (buffer?.contentHashCode() ?: 0)
        return result
    }
}