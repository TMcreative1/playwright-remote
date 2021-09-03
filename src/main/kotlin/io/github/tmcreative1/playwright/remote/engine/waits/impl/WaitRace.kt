package com.playwright.remote.engine.waits.impl

import com.playwright.remote.engine.waits.api.IWait

class WaitRace<T>(
    private val waits: List<IWait<T>>
) : IWait<T> {

    override fun isFinished(): Boolean {
        waits.forEach {
            if (it.isFinished()) {
                return true
            }
        }
        return false
    }

    override fun get(): T {
        assert(isFinished())
        dispose()
        waits.forEach {
            if (it.isFinished()) {
                return it.get()
            }
        }
        throw IllegalStateException("At least one element must be ready")
    }

    override fun dispose() = waits.forEach { it.dispose() }
}