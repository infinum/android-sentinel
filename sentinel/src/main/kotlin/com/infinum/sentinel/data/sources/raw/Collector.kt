package com.infinum.sentinel.data.sources.raw

interface Collector<T> {

    fun collect()

    fun present(): T
}