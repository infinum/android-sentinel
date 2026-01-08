package com.infinum.sentinel.di

import com.infinum.sentinel.di.component.DomainComponent
import com.infinum.sentinel.ui.certificates.observer.CertificateCheckWorker
import com.infinum.sentinel.ui.certificates.observer.DelegateWorker
import com.infinum.sentinel.ui.certificates.observer.SentinelWorkerFactory

internal object WorkManagerInitializer {
    fun init(domainComponent: DomainComponent) {
        DelegateWorker.workerFactories[CertificateCheckWorker.NAME] =
            SentinelWorkerFactory(domainComponent.collectors, domainComponent.notificationFactory)
    }
}
