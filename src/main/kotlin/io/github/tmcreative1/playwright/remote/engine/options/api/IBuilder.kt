package io.github.tmcreative1.playwright.remote.engine.options.api

fun interface IBuilder<T> {
    fun build(self: T)
}