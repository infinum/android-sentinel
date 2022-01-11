package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.infinum.sentinel.data.models.raw.certificates.CertificateType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.extensions.applicationName
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

internal class CertificatesObserver(
    private val context: Context,
    private val collectors: Factories.Collector,
    private val notificationFactory: NotificationFactory
) : LifecycleEventObserver {

    private val scope = MainScope()
    private var currentJob: Job? = null

    init {
        scope.launch(Dispatchers.Main) {
            ProcessLifecycleOwner.get().lifecycle.addObserver(this@CertificatesObserver)
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_START -> start()
            Lifecycle.Event.ON_STOP -> stop()
            else -> Unit
        }
    }

    private fun start() {
        currentJob?.cancel()
        currentJob = scope.launch(Dispatchers.Main) {
            val result = withContext(Dispatchers.IO) {
                suspendCancellableCoroutine<CertificateCount> {
                    val userCertificates = collectors.certificates()
                        .invoke()[CertificateType.USER]
                        .orEmpty()

                    val invalidCertificatesCount = userCertificates
                        .filterNot { certificate -> certificate.isValidNow }
                        .count()
                    val toExpireCertificatesCount = userCertificates
                        .filterNot { certificate -> certificate.isValidIn() }
                        .count()

                    it.resume(
                        CertificateCount(
                            invalidCertificatesCount,
                            toExpireCertificatesCount
                        )
                    )
                }
            }
            if (result.invalid > 0) {
                notificationFactory.showExpiredCertificate(context.applicationName, result.invalid)
            }
            if (result.toExpire > 0) {
                notificationFactory.showToExpireCertificate(context.applicationName, result.toExpire)
            }
        }
    }

    private fun stop() {
        currentJob?.cancel()
        currentJob = null
    }
}
