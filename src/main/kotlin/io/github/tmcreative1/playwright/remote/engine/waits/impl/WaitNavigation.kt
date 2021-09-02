package io.github.tmcreative1.playwright.remote.engine.waits.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.core.enums.InternalEventType
import io.github.tmcreative1.playwright.remote.core.enums.InternalEventType.NAVIGATED
import io.github.tmcreative1.playwright.remote.core.enums.LoadState
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection
import io.github.tmcreative1.playwright.remote.engine.listener.UniversalConsumer
import io.github.tmcreative1.playwright.remote.engine.processor.MessageProcessor
import io.github.tmcreative1.playwright.remote.engine.route.UrlMatcher
import io.github.tmcreative1.playwright.remote.engine.route.request.api.IRequest
import io.github.tmcreative1.playwright.remote.engine.route.request.impl.Request
import io.github.tmcreative1.playwright.remote.engine.route.response.api.IResponse
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait

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

    init {
        @Suppress("UNCHECKED_CAST")
        internalListeners.add(NAVIGATED, this as UniversalConsumer)
    }

    @Suppress("UNCHECKED_CAST")
    override fun invoke(p1: JsonObject) {
        if (!matcher.test(p1["url"].asString)) {
            return
        }
        if (p1.has("error")) {
            exception = PlaywrightException(p1["error"].asString)
        } else {
            if (p1.has("newDocument")) {
                val jsonRequest = p1["newDocument"].asJsonObject["request"]
                if (jsonRequest != null) {
                    request = messageProcessor.getExistingObject(jsonRequest.asJsonObject["guid"].asString)
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