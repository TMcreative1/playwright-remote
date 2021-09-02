package io.github.tmcreative1.playwright.remote.engine.waits.impl

import io.github.tmcreative1.playwright.remote.core.exceptions.TimeoutException
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait

open class WaitTimeout<T>(millis: Double) : IWait<T> {
    private val deadline: Long = System.nanoTime() + millis.toLong() * 1_000_000
    private val timeout: Double = millis
    override fun isFinished(): Boolean = System.nanoTime() > deadline

    override fun get(): T {
        var timeoutStr = timeout.toString()
        if (timeoutStr.endsWith(".0")) {
            timeoutStr = timeoutStr.substring(0, timeoutStr.length - 2)
        }
        throw TimeoutException("Timeout $timeoutStr ms exceeded")
    }

    override fun dispose() {
    }
}