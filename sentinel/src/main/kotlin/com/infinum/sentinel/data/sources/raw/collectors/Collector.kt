package com.infinum.sentinel.data.sources.raw.collectors

internal interface Collector<T> {

    operator fun invoke(): T
}
