package com.infinum.sentinel.data.sources.raw

interface Collector<T> {

    operator fun invoke(): T
}
