package com.infinum.sentinel.domain.networkproxy.models

internal enum class RuleMode {
    MODIFY,
    MOCK,
    ;

    companion object {
        fun fromName(name: String?): RuleMode = entries.firstOrNull { it.name == name } ?: MODIFY
    }
}
