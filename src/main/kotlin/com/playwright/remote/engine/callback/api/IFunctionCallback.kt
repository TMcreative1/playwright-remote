package com.playwright.remote.engine.callback.api

fun interface IFunctionCallback {
    fun call(args: Array<Any>) : Any
}