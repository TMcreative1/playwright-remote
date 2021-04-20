package com.playwright.remote.callback.impl

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.playwright.remote.callback.api.IBindingCall
import com.playwright.remote.callback.api.IBindingCallback
import com.playwright.remote.domain.serialize.SerializedError
import com.playwright.remote.engine.browser.api.IBrowserContext
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.page.api.IPage
import com.playwright.remote.engine.parser.IParser.Companion.fromJson
import com.playwright.remote.engine.processor.ChannelOwner
import com.playwright.remote.engine.serializer.Serialization.Companion.deserialize
import com.playwright.remote.engine.serializer.Serialization.Companion.serializeArgument
import com.playwright.remote.engine.serializer.Serialization.Companion.serializeError

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
            params.add("result", Gson().toJsonTree(serializeArgument(result)))
            sendMessage("resolve", params)
        } catch (e: RuntimeException) {
            val params = JsonObject()
            params.add("error", Gson().toJsonTree(serializeError(e)))
            sendMessage("reject", params)
        }
    }
}