package playwright.waits.impl

import core.exceptions.PlaywrightException
import core.exceptions.TimeoutException
import playwright.waits.api.IWait

class WaitResult<T> : IWait<T> {

    private var result: T? = null
    private var exception: RuntimeException? = null
    private var isFinished: Boolean = false

    fun complete(_result: T) {
        if (isFinished) {
            return
        }
        result = _result
        isFinished = true
    }

    fun completeWithException(_exception: RuntimeException) {
        if (isFinished) {
            return
        }
        exception = _exception
        isFinished = true
    }

    override fun isFinished(): Boolean {
        return isFinished
    }

    override fun get(): T = when (exception) {
        is TimeoutException -> throw TimeoutException(exception?.message, exception)
        is PlaywrightException -> throw PlaywrightException(exception?.message, exception)
        else -> result!!
    }

    override fun dispose() {
    }
}