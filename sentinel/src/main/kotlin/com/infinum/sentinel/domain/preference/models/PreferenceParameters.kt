package com.infinum.sentinel.domain.preference.models

import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.domain.shared.base.BaseParameters

internal sealed class PreferenceParameters(
    open val name: String,
    open val key: String,
) : BaseParameters {
    data class Cache(
        override val name: String,
        override val key: String,
        val value: Triple<PreferenceType, String, Any>,
    ) : PreferenceParameters(name, key)

    data class BooleanType(
        override val name: String,
        override val key: String,
        val value: Boolean,
    ) : PreferenceParameters(name, key)

    data class FloatType(
        override val name: String,
        override val key: String,
        val value: Float,
    ) : PreferenceParameters(name, key)

    data class IntType(
        override val name: String,
        override val key: String,
        val value: Int,
    ) : PreferenceParameters(name, key)

    data class LongType(
        override val name: String,
        override val key: String,
        val value: Long,
    ) : PreferenceParameters(name, key)

    data class StringType(
        override val name: String,
        override val key: String,
        val value: String,
    ) : PreferenceParameters(name, key)

    data class ArrayType(
        override val name: String,
        override val key: String,
        val value: Array<String>,
    ) : PreferenceParameters(name, key) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ArrayType

            if (name != other.name) return false
            if (key != other.key) return false
            if (!value.contentEquals(other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + key.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }
}
