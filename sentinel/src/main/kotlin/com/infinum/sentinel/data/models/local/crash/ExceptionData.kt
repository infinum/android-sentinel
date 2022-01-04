package com.infinum.sentinel.data.models.local.crash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExceptionData(

    @SerialName("message")
    val message: String?,

    @SerialName("name")
    val name: String?,

    @SerialName("file")
    val file: String?,

    @SerialName("lineNumber")
    val lineNumber: Int,

    @SerialName("stack_trace")
    val stackTrace: List<String>,

    @SerialName("is_ANR_exception")
    val isANRException: Boolean = false
) {

    fun asPrint(spacer: String = "\n\t\t\t"): String =
        "${name}: $message".plus(stackTrace.joinToString { "$spacer at $it" })
}
