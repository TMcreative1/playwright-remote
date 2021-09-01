package io.github.tmcreative1.playwright.remote.engine.download.stream.api

import java.io.InputStream

interface IStream {
    fun stream(): InputStream
}