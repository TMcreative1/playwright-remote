package io.github.tmcreative1.playwright.remote.engine.serialize.serializers

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import io.github.tmcreative1.playwright.remote.core.enums.Media
import io.github.tmcreative1.playwright.remote.engine.options.ViewportSize
import io.github.tmcreative1.playwright.remote.engine.options.enum.ColorScheme
import java.lang.reflect.Type
import java.util.*

class OptionalSerializer : JsonSerializer<Optional<Any>> {
    override fun serialize(src: Optional<Any>?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        assert(isSupported(typeOfSrc!!)) { "Unexpected optional type: ${typeOfSrc.typeName}" }
        if (!src!!.isPresent) {
            return JsonPrimitive("null")
        }
        return context!!.serialize(src.get())
    }

    private fun isSupported(type: Type): Boolean {
        return isGenericTypeNameEquals<Optional<Media>>(type) ||
                isGenericTypeNameEquals<Optional<ColorScheme>>(type) ||
                isGenericTypeNameEquals<Optional<ViewportSize>>(type)

    }

    private inline fun <reified T> isGenericTypeNameEquals(type: Type) =
        object : TypeToken<T>() {}.type.typeName.equals(type.typeName)
}