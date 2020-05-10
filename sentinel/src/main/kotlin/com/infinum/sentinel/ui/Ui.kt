package com.infinum.sentinel.ui

import android.content.Context
import android.content.Intent
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.Domain

object Ui {

    private lateinit var context: Context

    fun initialise(context: Context) {
        this.context = context.applicationContext

        Domain.initialise(this.context)
    }

    fun setup(tools: Set<Sentinel.Tool>, onTriggered: () -> Unit) {
        Domain.setup(tools, onTriggered)
    }

    fun show() =
        context.startActivity(
            Intent(context, SentinelActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
        )
}
