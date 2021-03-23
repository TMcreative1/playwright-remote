package com.playwright.remote.engine.waits.impl

import com.playwright.remote.engine.waits.api.IWait

class WaitNever<T> : IWait<T> {
    override fun isFinished(): Boolean = false

    override fun get(): T = throw IllegalStateException("Should never be called")

    override fun dispose() {
    }
}