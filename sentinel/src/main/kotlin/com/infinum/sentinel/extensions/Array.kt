package com.infinum.sentinel.extensions

internal fun Array<StackTraceElement>.asStringArray(): List<String> =
    map {
        if (it.isNativeMethod) {
            "${it.className}.${it.methodName}[Native Method]"
        } else {
            "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})"
        }
    }
