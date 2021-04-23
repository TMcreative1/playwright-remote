package com.playwright.remote.engine.download.stream.api

import java.io.InputStream

interface IStream {
    fun stream(): InputStream
}