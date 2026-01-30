package com.infinum.sentinel.extensions

import com.infinum.sentinel.data.models.local.crash.ExceptionData

internal fun Throwable.asExceptionData(isANR: Boolean = false): ExceptionData =
    ExceptionData(
        name = this.toString().replace(": $message", "", true),
        message = message,
        stackTrace = stackTrace.asStringArray(),
        file = stackTrace.getOrNull(0)?.fileName,
        lineNumber = stackTrace.getOrNull(0)?.lineNumber ?: Int.MIN_VALUE,
        isANRException = isANR,
    )
