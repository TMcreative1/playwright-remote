package com.playwright.remote.playwright.browser.api

import com.playwright.remote.playwright.page.api.IPage

interface IBrowserContext : AutoCloseable {
    fun newPage(): IPage
}