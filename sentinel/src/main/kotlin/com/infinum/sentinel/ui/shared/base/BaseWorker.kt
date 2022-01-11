package com.infinum.sentinel.ui.shared.base

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.infinum.sentinel.di.LibraryKoinComponent

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), LibraryKoinComponent
