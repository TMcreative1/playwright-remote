package com.playwright.remote.engine.callback.api

interface IBindingCall {
    fun name(): String
    fun call(binding: IBindingCallback)
}