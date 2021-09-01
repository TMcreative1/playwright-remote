package io.github.tmcreative1.playwright.remote.engine.callback.api

interface IBindingCall {
    fun name(): String
    fun call(binding: IBindingCallback)
}