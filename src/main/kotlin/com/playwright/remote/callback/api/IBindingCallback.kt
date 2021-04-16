package com.playwright.remote.callback.api

import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.page.api.IPage

fun interface IBindingCallback {
    interface ISource {
        fun context(): IBrowserContext
        fun page(): IPage
        fun frame(): IFrame
    }

    fun call(source: ISource, vararg args: Any): Any
}