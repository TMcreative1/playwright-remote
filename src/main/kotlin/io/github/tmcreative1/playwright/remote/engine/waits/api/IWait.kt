package io.github.tmcreative1.playwright.remote.engine.waits.api

interface IWait<T> {
    fun isFinished() : Boolean
    fun get() : T
    fun dispose()
}