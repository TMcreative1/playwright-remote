package io.github.tmcreative1.playwright.remote.engine.waits.impl

import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait

open class WaitNever<T> : IWait<T> {
    override fun isFinished(): Boolean = false

    override fun get(): T = throw IllegalStateException("Should never be called")

    override fun dispose() {
    }
}