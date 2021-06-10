package com.playwright.remote.engine.playwright.impl

import com.playwright.remote.engine.selector.api.ISharedSelectors

interface IPlaywright {
    fun initSharedSelectors(parent: IPlaywright?)

    fun unregisterSelectors()

    fun selectors(): ISharedSelectors?
}