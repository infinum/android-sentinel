package com.infinum.sentinel.data.sources.raw.collectors

internal interface Collector<T> {
    operator fun invoke(): T

    operator fun invoke(filter: Boolean): T = invoke()
}
