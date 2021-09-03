package com.playwright.remote.domain.message

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.playwright.remote.domain.serialize.SerializedError

data class Message(
    val id: Int,
    val guid: String,
    val method: String?,
    val params: JsonObject,
    val result: JsonElement,
    val error: SerializedError?
) {

    override fun toString(): String {
        return "Message(\nid=$id, \nguid='$guid', \nmethod='$method', \nparams=$params, \nresult=$result, \nerror=$error\n)"
    }

}