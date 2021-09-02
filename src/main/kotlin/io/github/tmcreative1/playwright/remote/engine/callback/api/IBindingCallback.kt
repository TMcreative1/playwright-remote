package io.github.tmcreative1.playwright.remote.engine.callback.api

import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowserContext
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage

fun interface IBindingCallback {
    interface ISource {
        fun context(): IBrowserContext?
        fun page(): IPage?
        fun frame(): IFrame
    }

    fun call(source: ISource, args: Array<Any>): Any
}