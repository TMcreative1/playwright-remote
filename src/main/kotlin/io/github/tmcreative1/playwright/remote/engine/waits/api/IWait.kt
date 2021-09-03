package com.playwright.remote.engine.waits.api

interface IWait<T> {
    fun isFinished() : Boolean
    fun get() : T
    fun dispose()
}