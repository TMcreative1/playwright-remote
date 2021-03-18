package com.playwright.remote.playwright.waits.api

interface IWait<T> {
    fun isFinished() : Boolean
    fun get() : T
    fun dispose()
}