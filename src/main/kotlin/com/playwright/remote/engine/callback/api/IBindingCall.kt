package com.playwright.remote.callback.api

interface IBindingCall {
    fun name(): String
    fun call(binding: IBindingCallback)
}