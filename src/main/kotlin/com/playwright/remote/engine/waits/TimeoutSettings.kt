package com.playwright.remote.engine.waits

import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.WaitNever
import com.playwright.remote.engine.waits.impl.WaitTimeout

class TimeoutSettings(private val parent: TimeoutSettings? = null) {
    private val defaultTimeoutMs = (30_000).toDouble()

    var defaultTimeout = defaultTimeoutMs
    var defaultNavigationTimeout = defaultTimeoutMs

    fun timeout(timeout: Double?): Double = when {
        timeout != null -> timeout
        defaultTimeout != defaultTimeoutMs -> defaultTimeout.toDouble()
        parent != null -> parent.timeout(timeout)
        else -> defaultTimeoutMs
    }

    fun navigationTimout(timeout: Double?): Double = when {
        timeout != null -> timeout
        defaultNavigationTimeout != defaultTimeoutMs -> defaultNavigationTimeout
        defaultTimeout != defaultTimeoutMs -> defaultTimeout
        parent != null -> parent.navigationTimout(timeout)
        else -> defaultTimeoutMs
    }

    fun <T> createWait(timeout: Double?): IWait<T> {
        if (timeout != null && timeout == 0.0) {
            return WaitNever()
        }
        return WaitTimeout(timeout(timeout))
    }
}