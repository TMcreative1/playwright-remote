package io.github.tmcreative1.playwright.remote.engine.route

import io.github.tmcreative1.playwright.remote.domain.request.HttpHeader

class RawHeader(private val headersArray: List<HttpHeader>) {
    private val headersMap = linkedMapOf<String, MutableList<String>>()

    init {
        headersArray.forEach {
            val name = it.name.lowercase()
            var values = headersMap[name]
            if (values == null) {
                values = arrayListOf()
                headersMap[name] = values
            }
            values.add(it.value)
        }
    }

    fun get(name: String): String? {
        val values = getAll(name) ?: return null
        return values.joinToString(separator = if ("set-cookie" == name.lowercase()) "\n" else ", ")
    }

    fun getAll(name: String): MutableList<String>? = headersMap[name.lowercase()]

    fun headers(): Map<String, String?> {
        val result = hashMapOf<String, String?>()
        headersMap.keys.forEach {
            result[it] = get(it)
        }
        return result
    }

    fun headersArray(): List<HttpHeader> = headersArray
}