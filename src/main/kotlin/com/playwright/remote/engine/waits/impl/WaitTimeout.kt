package com.playwright.remote.engine.waits.impl

import com.playwright.remote.core.exceptions.TimeoutException
import com.playwright.remote.engine.waits.api.IWait

open class WaitTimeout<T>(millis: Double) : IWait<T> {
    private val deadline: Long = millis.toLong()
    private val timeout: Double = (System.nanoTime() + millis.toLong() * 1_000_000).toDouble()

    override fun isFinished(): Boolean = System.nanoTime() > deadline

    override fun get(): T {
        var timeoutStr = timeout.toString()
        if (timeoutStr.endsWith(".0")) {
            timeoutStr = timeoutStr.substring(0, timeoutStr.length - 2)
        }
        throw TimeoutException("Timeout ${timeoutStr} ms exceeded")
    }

    override fun dispose() {
    }
}