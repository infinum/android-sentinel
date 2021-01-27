package com.infinum.sentinel.data.sources.raw.collectors

interface Collector<T> {

    operator fun invoke(): T
}
