package com.playwright.remote.engine.waits

import com.playwright.remote.engine.waits.api.IWait
import com.playwright.remote.engine.waits.impl.WaitNever
import com.playwright.remote.engine.waits.impl.WaitTimeout

class TimeoutSettings(_parent: TimeoutSettings?) {
    private val defaultTimeoutMs = (30_000).toDouble()

    private var parent: TimeoutSettings? = _parent
    private var defaultTimeout = defaultTimeoutMs
    private var defaultNavigationTimeout = defaultTimeoutMs

    fun setDefaultTimeout(timeout: Double) {
        defaultTimeout = timeout
    }

    fun setDefaultNavigationTimeout(timeout: Double) {
        defaultNavigationTimeout = timeout
    }

    fun timeout(timeout: Double?): Double = when {
        timeout != null -> timeout
        defaultTimeout != defaultTimeoutMs -> defaultTimeout.toDouble()
        parent != null -> parent!!.timeout(timeout)
        else -> defaultTimeoutMs
    }

    fun navigationTimout(timeout: Double?): Double = when {
        timeout != null -> timeout
        defaultNavigationTimeout != defaultTimeoutMs -> defaultNavigationTimeout
        defaultTimeout != defaultTimeoutMs -> defaultTimeout
        parent != null -> parent!!.navigationTimout(timeout)
        else -> defaultTimeoutMs
    }

    fun <T> createWait(timeout: Double?): IWait<T> {
        if (timeout != null && timeout == 0.0) {
            return WaitNever()
        }
        return WaitTimeout(timeout(timeout))
    }
}