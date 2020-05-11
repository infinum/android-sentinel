package com.infinum.sentinel.ui

import android.content.Context
import android.content.Intent
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.DomainGraph

internal object DependencyGraph {

    private lateinit var context: Context

    private lateinit var domain: DomainGraph

    fun initialise(context: Context) {
        this.context = context
        this.domain = DomainGraph(context)
    }

    fun setup(tools: Set<Sentinel.Tool>, onTriggered: () -> Unit) {
        domain.setup(tools, onTriggered)
    }

    fun show() =
        context.startActivity(
            Intent(context, SentinelActivity::class.java)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
        )

    fun collectors() = domain.collectors()

    fun formatters() = domain.formatters()

    fun formats() = domain.formats()

    fun triggers() = domain.triggers()
}
