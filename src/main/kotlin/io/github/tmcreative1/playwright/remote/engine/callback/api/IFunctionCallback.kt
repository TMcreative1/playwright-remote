package io.github.tmcreative1.playwright.remote.engine.callback.api

fun interface IFunctionCallback {
    fun call(args: Array<Any>) : Any
}