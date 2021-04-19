package com.playwright.remote.engine.options

import com.playwright.remote.engine.options.api.IBuilder
import java.nio.file.Path

data class FulfillOptions @JvmOverloads constructor(
    /**
     * Optional response body as text.
     */
    var body: String? = null,
    /**
     * Optional response body as raw bytes.
     */
    var bodyBytes: ByteArray? = null,
    /**
     * If set, equals to setting {@code Content-Type} response header.
     */
    var contentType: String? = null,
    /**
     * Response headers. Header values will be converted to a string.
     */
    var headers: Map<String, String>? = null,
    /**
     * File path to respond with. The content type will be inferred from file extension. If {@code path} is a relative path, then it
     * is resolved relative to the current working directory.
     */
    var path: Path? = null,
    /**
     * Response status code, defaults to {@code 200}.
     */
    var status: Int? = null,
    private val builder: IBuilder<FulfillOptions>
) {
    init {
        builder.build(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FulfillOptions

        if (body != other.body) return false
        if (bodyBytes != null) {
            if (other.bodyBytes == null) return false
            if (!bodyBytes.contentEquals(other.bodyBytes)) return false
        } else if (other.bodyBytes != null) return false
        if (contentType != other.contentType) return false
        if (headers != other.headers) return false
        if (path != other.path) return false
        if (status != other.status) return false
        if (builder != other.builder) return false

        return true
    }

    override fun hashCode(): Int {
        var result = body?.hashCode() ?: 0
        result = 31 * result + (bodyBytes?.contentHashCode() ?: 0)
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + (headers?.hashCode() ?: 0)
        result = 31 * result + (path?.hashCode() ?: 0)
        result = 31 * result + (status ?: 0)
        result = 31 * result + builder.hashCode()
        return result
    }
}