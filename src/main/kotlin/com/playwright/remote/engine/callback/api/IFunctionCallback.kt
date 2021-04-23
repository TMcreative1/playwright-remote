package com.playwright.remote.engine.callback.api

fun interface IFunctionCallback {
    fun call(vararg args: Any) : Any
}