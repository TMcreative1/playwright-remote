package com.playwright.remote.engine.route

import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.utils.Utils.Companion.globToRegex
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import java.util.regex.Pattern.compile

typealias Predicate = (String) -> Boolean

class UrlMatcher(private val rawSource: Any = "", private val predicate: Predicate? = null) {

    companion object {
        private fun toPredicate(pattern: Pattern): Predicate = { str -> pattern.matcher(str).find() }

        private fun any(): UrlMatcher = UrlMatcher()

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun forOneOf(baseUrl: URL?, obj: Any?): UrlMatcher = when (obj) {
            null -> any()
            is String -> UrlMatcher(baseUrl, obj)
            is Pattern -> UrlMatcher(obj)
            is Function<*> -> UrlMatcher(obj as Predicate)
            else -> throw PlaywrightException("Url must be String, Pattern or Predicate, found: ${obj.javaClass.name}")
        }

        private fun resolveUrl(baseUrl: URL?, spec: String): String {
            if (baseUrl == null) {
                return spec
            }
            return try {
                URL(baseUrl, spec).toString()
            } catch (e: MalformedURLException) {
                spec
            }
        }
    }

    constructor(pattern: Pattern) : this(pattern, toPredicate(pattern))

    constructor(predicate: Predicate) : this(predicate, predicate)

    constructor(url: String) : this(
        url,
        toPredicate(compile(globToRegex(url))).also { str -> url == "" || url.equals(str) })

    constructor(baseUrl: URL?, url: String) : this(
        url,
        toPredicate(compile(globToRegex(resolveUrl(baseUrl, url)))).also { str -> url == "" || url.equals(str) }
    )

    fun test(value: String) = predicate == null || predicate.invoke(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is UrlMatcher) {
            return false
        }

        return rawSource == other.rawSource
    }

    override fun hashCode(): Int {
        return Objects.hashCode(rawSource)
    }
}