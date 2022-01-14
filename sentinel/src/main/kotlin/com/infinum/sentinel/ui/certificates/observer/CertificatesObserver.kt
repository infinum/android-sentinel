package com.infinum.sentinel.ui.certificates.observer

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.data.models.raw.certificates.CertificateType
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.extensions.applicationName
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import java.time.temporal.ChronoUnit
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

    private var active = false
    private var notifyInvalidNow = false
    private var notifyToExpire = false
    private var expireInAmount = 0
    private var expireInUnit = ChronoUnit.DAYS

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

    fun activate(entity: CertificateMonitorEntity) {
        active = true
        this.notifyInvalidNow = entity.notifyInvalidNow
        this.notifyToExpire = entity.notifyToExpire
        this.expireInAmount = entity.expireInAmount
        this.expireInUnit = entity.expireInUnit
    }

    fun deactivate() {
        active = false
    }

    private fun start() {
        stop()

        if (notifyInvalidNow || notifyToExpire) {
            currentJob = scope.launch(Dispatchers.Main) {
                val result = withContext(Dispatchers.IO) {
                    if (active) {
                        suspendCancellableCoroutine {
                            val userCertificates = collectors.certificates()
                                .invoke()[CertificateType.USER]
                                .orEmpty()

                            val invalidCertificatesCount = userCertificates
                                .filterNot { certificate -> certificate.isValidNow }
                                .count()
                            val toExpireCertificatesCount = userCertificates
                                .filterNot { certificate -> certificate.isValidIn(expireInAmount, expireInUnit) }
                                .count()

                            it.resume(
                                CertificateCount(
                                    invalidCertificatesCount,
                                    toExpireCertificatesCount
                                )
                            )
                        }
                    } else {
                        CertificateCount(0, 0)
                    }
                }
                if (result.invalid > 0 && notifyInvalidNow) {
                    notificationFactory.showExpiredCertificate(context.applicationName, result.invalid)
                }
                if (result.toExpire > 0 && notifyToExpire) {
                    notificationFactory.showToExpireCertificate(context.applicationName, result.toExpire)
                }
            }
        }
        // dont run
    }

    private fun stop() {
        currentJob?.cancel()
        currentJob = null
    }
}
