package com.infinum.sentinel.domain

import android.content.Context
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.Data
import com.infinum.sentinel.domain.repository.CollectorRepository
import com.infinum.sentinel.domain.repository.FormatterRepository

object Domain {

    fun initialise(context: Context) {
        Data.initialise(context)

        CollectorRepository.initialise(context)
        FormatterRepository.initialise(context)
    }

    fun setup(tools: Set<Sentinel.Tool>, onTriggered: () -> Unit) {
        Data.setup(onTriggered)

        CollectorRepository.setup(tools)
    }
}
