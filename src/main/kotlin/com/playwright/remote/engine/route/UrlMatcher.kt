package com.playwright.remote.engine.route

class UrlMatcher(
    private val rawSource: Any,
    private val predicate: (String) -> Boolean
) {

}