package com.playwright.remote.callback.api

fun interface IFunctionCallback {
    fun call(vararg args: Any) : Any
}