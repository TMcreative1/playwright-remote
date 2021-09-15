package io.github.tmcreative1.playwright.remote.engine.serialize.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.github.tmcreative1.playwright.remote.core.enums.SameSiteAttribute
import io.github.tmcreative1.playwright.remote.core.enums.SameSiteAttribute.*
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException

class SameSiteAttributeAdapter : TypeAdapter<SameSiteAttribute>() {
    override fun write(out: JsonWriter?, value: SameSiteAttribute?) {
        out?.value(
            when (value) {
                STRICT -> "Strict"
                LAX -> "Lax"
                NONE -> "None"
                else -> throw PlaywrightException("Unexpected value: $value")
            }
        )
    }

    override fun read(`in`: JsonReader?): SameSiteAttribute =
        valueOf(`in`!!.nextString().uppercase())
}