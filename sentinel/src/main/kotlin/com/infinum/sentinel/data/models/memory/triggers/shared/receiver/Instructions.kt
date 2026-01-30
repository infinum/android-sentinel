package com.infinum.sentinel.data.models.memory.triggers.shared.receiver

import android.content.Intent

internal typealias Execution = (Intent) -> Unit

internal sealed class Instructions {
    abstract fun matches(intent: Intent): Boolean

    abstract fun execution(): Execution

    data class OnAction(
        val action: String,
        val execution: Execution,
    ) : Instructions() {
        override fun matches(intent: Intent): Boolean = intent.action == action

        override fun execution() = execution
    }

    data class OnDataScheme(
        val scheme: String,
        val execution: Execution,
    ) : Instructions() {
        override fun matches(intent: Intent): Boolean = intent.data?.scheme == scheme

        override fun execution() = execution
    }

    data class OnCategory(
        val category: String,
        val execution: Execution,
    ) : Instructions() {
        override fun matches(intent: Intent): Boolean = intent.hasCategory(category)

        override fun execution() = execution
    }
}
