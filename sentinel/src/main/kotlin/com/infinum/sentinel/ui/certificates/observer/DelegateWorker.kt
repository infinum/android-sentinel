package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import androidx.annotation.UiThread
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.common.util.concurrent.ListenableFuture
import com.infinum.sentinel.ui.shared.Constants.Keys.WORKER_CLASS_NAME
import com.infinum.sentinel.ui.shared.Constants.Keys.WORKER_ID

/**
 * A worker to delegate work requests from within the library to workers
 * that require factories with custom dependencies.
 */
internal class DelegateWorker(
    appContext: Context,
    parameters: WorkerParameters,
) : ListenableWorker(appContext, parameters) {

    private val workerClassName =
        parameters.inputData.getString(WORKER_CLASS_NAME) ?: ""
    private val workerId = parameters.inputData.getString(WORKER_ID)
    private val delegateWorkerFactory = workerFactories[workerId]
    private val delegatedWorker = delegateWorkerFactory?.createWorker(appContext, workerClassName, parameters)

    override fun startWork(): ListenableFuture<Result> {
        return if (delegatedWorker != null) {
            delegatedWorker.startWork()
        } else {
            val errorMessage = "No delegateWorker available for $workerId" +
                " with workerClassName of $workerClassName. Is the " +
                "DelegateWorker.workerFactories populated correctly?"
            throw IllegalStateException(errorMessage)
        }
    }

    companion object {
        const val DELEGATE_WORKER_ID = "com.infinum.sentinel.ui.certificates.observer.CertificateCheckWorker"

        val workerFactories = object : AbstractMutableMap<String, WorkerFactory>() {

            private val backingWorkerMap = mutableMapOf<String, WorkerFactory>()

            @UiThread
            override fun put(key: String, value: WorkerFactory): WorkerFactory? {
                return backingWorkerMap.put(key, value)
            }

            override val entries: MutableSet<MutableMap.MutableEntry<String, WorkerFactory>>
                get() = backingWorkerMap.entries
        }
    }
}
