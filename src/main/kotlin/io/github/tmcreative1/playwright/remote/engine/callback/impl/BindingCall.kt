package io.github.tmcreative1.playwright.remote.engine.callback.impl

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.domain.serialize.SerializedError
import io.github.tmcreative1.playwright.remote.engine.browser.api.IBrowserContext
import io.github.tmcreative1.playwright.remote.engine.callback.api.IBindingCall
import io.github.tmcreative1.playwright.remote.engine.callback.api.IBindingCallback
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.handle.js.api.IJSHandle
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.fromJson
import io.github.tmcreative1.playwright.remote.engine.processor.ChannelOwner
import io.github.tmcreative1.playwright.remote.engine.serialize.CustomGson.Companion.gson
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.deserialize
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.serializeArgument
import io.github.tmcreative1.playwright.remote.engine.serialize.Serialization.Companion.serializeError

class BindingCall(parent: ChannelOwner, type: String, guid: String, initializer: JsonObject) :
    ChannelOwner(parent, type, guid, initializer), IBindingCall {
    private class Source(private val frame: IFrame) : IBindingCallback.ISource {
        override fun context(): IBrowserContext? {
            return page()?.context()
        }

        override fun page(): IPage? {
            return frame.page()
        }

        override fun frame(): IFrame {
            return frame
        }
    }

    override fun name(): String {
        return initializer["name"].asString
    }

    override fun call(binding: IBindingCallback) {
        try {
            val frame = messageProcessor.getExistingObject<IFrame>(initializer["frame"].asJsonObject["guid"].asString)
            val source = Source(frame)
            val args = arrayListOf<Any>()
            if (initializer.has("handle")) {
                val handle =
                    messageProcessor.getExistingObject<IJSHandle>(initializer["handle"].asJsonObject["guid"].asString)
                args.add(handle)
            } else {
                for (arg in initializer["args"].asJsonArray) {
                    args.add(
                        deserialize(
                            fromJson(
                                arg,
                                SerializedError.SerializedValue::class.java
                            )
                        )
                    )
                }
            }
            val result = binding.call(source, args.toArray())

            val params = JsonObject()
            params.add("result", gson().toJsonTree(serializeArgument(result)))
            sendMessage("resolve", params)
        } catch (e: RuntimeException) {
            val params = JsonObject()
            params.add("error", gson().toJsonTree(serializeError(e)))
            sendMessage("reject", params)
        }
    }
}