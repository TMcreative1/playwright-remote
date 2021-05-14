package com.playwright.remote.engine.waits.impl

import com.google.gson.JsonObject
import com.playwright.remote.core.enums.InternalEventType
import com.playwright.remote.core.enums.InternalEventType.NAVIGATED
import com.playwright.remote.core.enums.LoadState
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.processor.MessageProcessor
import com.playwright.remote.engine.route.UrlMatcher
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.request.impl.Request
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.engine.waits.api.IWait

class WaitNavigation(
    private val matcher: UrlMatcher,
    private val expectedLoadState: LoadState,
    private val internalListeners: ListenerCollection<InternalEventType>,
    private val messageProcessor: MessageProcessor,
    private val loadStates: Set<LoadState>
) : IWait<IResponse?>, (JsonObject) -> Unit {
    private var waitLoadState: WaitLoadState? = null
    private var request: IRequest? = null
    private var exception: RuntimeException? = null

    @Suppress("UNCHECKED_CAST")
    override fun invoke(p1: JsonObject) {
        if (!matcher.test(p1["url"].asString)) {
            return
        }
        if (p1.has("error")) {
            exception = PlaywrightException(p1["error"].asString)
        } else {
            if (p1.has("newDocument")) {
                val jsonRequest = p1["newDocument"].asJsonObject["request"].asJsonObject
                if (jsonRequest != null) {
                    request = messageProcessor.getExistingObject(jsonRequest["guid"].asString)
                }
            }
            waitLoadState = WaitLoadState(expectedLoadState, internalListeners, loadStates)
        }
        internalListeners.remove(NAVIGATED, this as UniversalConsumer)
    }

    override fun isFinished(): Boolean {
        if (exception != null) {
            return true
        }
        if (waitLoadState != null) {
            return waitLoadState!!.isFinished()
        }
        return false
    }

    override fun get(): IResponse? {
        if (exception != null) {
            throw exception!!
        }
        if (request == null) {
            return null
        }
        return (request as Request).finalRequest().response()
    }

    @Suppress("UNCHECKED_CAST")
    override fun dispose() {
        internalListeners.remove(NAVIGATED, this as UniversalConsumer)
        if (waitLoadState != null) {
            waitLoadState!!.dispose()
        }
    }
}