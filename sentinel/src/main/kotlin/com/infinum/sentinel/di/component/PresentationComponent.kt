package com.infinum.sentinel.di.component

import android.content.Context
import android.content.Intent
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.di.scope.PresentationScope
import com.infinum.sentinel.ui.main.SentinelActivity
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@PresentationScope
internal abstract class PresentationComponent(
    @get:Provides val context: Context,
    @Component val viewModelComponent: ViewModelComponent
) {

    abstract val launchIntent: Intent

    @Provides
    @PresentationScope
    fun launchIntent(): Intent =
        Intent(context, SentinelActivity::class.java)
            .apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }

    fun show() =
        context.startActivity(launchIntent)

    fun setExceptionHandler(handler: Thread.UncaughtExceptionHandler?) =
        viewModelComponent
            .domainComponent
            .sentinelExceptionHandler
            .setExceptionHandler(handler)

    fun setAnrListener(listener: Sentinel.ApplicationNotRespondingListener?) =
        viewModelComponent
            .domainComponent
            .sentinelAnrObserver
            .setListener(listener)
}
