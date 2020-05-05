package com.infinum.sentinel.sample

import android.app.Application
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.sample.tools.SentinelTools

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Sentinel.watch(this, SentinelTools.get())
    }
}