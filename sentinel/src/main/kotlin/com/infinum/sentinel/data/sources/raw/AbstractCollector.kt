package com.infinum.sentinel.data.sources.raw

internal abstract class AbstractCollector<T> :
    Collector<T> {

    internal abstract var data: T

    override fun present(): T = data
}