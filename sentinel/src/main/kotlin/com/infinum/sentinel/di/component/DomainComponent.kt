package com.infinum.sentinel.di.component

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.format.Formatter
import com.google.android.material.snackbar.Snackbar
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.memory.triggers.TriggerType
import com.infinum.sentinel.data.sources.local.room.dao.BundleMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CertificateMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CrashMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao
import com.infinum.sentinel.data.sources.local.room.dao.TriggersDao
import com.infinum.sentinel.data.sources.memory.bundles.BundlesCache
import com.infinum.sentinel.data.sources.memory.certificate.CertificateCache
import com.infinum.sentinel.data.sources.memory.preference.PreferenceCache
import com.infinum.sentinel.data.sources.memory.targetedpreferences.TargetedPreferencesCache
import com.infinum.sentinel.data.sources.memory.triggers.TriggersCache
import com.infinum.sentinel.di.scope.DomainScope
import com.infinum.sentinel.domain.Factories
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.BundlesRepository
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.BundleMonitorRepository
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.certificate.CertificateRepository
import com.infinum.sentinel.domain.certificate.monitor.CertificateMonitorRepository
import com.infinum.sentinel.domain.certificate.monitor.models.CertificateMonitorParameters
import com.infinum.sentinel.domain.collectors.CollectorFactory
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.crash.monitor.CrashMonitorRepository
import com.infinum.sentinel.domain.crash.monitor.models.CrashMonitorParameters
import com.infinum.sentinel.domain.formats.FormatsRepository
import com.infinum.sentinel.domain.formatters.FormatterFactory
import com.infinum.sentinel.domain.formatters.Formatters
import com.infinum.sentinel.domain.preference.PreferenceRepository
import com.infinum.sentinel.domain.targetedpreferences.TargetedPreferencesRepository
import com.infinum.sentinel.domain.triggers.TriggersRepository
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import com.infinum.sentinel.extensions.enableAirplaneModeOnTrigger
import com.infinum.sentinel.extensions.enableForegroundTrigger
import com.infinum.sentinel.extensions.enableProximityTrigger
import com.infinum.sentinel.extensions.enableShakeTrigger
import com.infinum.sentinel.extensions.enableUsbConnectedTrigger
import com.infinum.sentinel.extensions.sizeTree
import com.infinum.sentinel.ui.bundles.callbacks.BundleMonitorActivityCallbacks
import com.infinum.sentinel.ui.bundles.callbacks.BundleMonitorNotificationCallbacks
import com.infinum.sentinel.ui.bundles.details.BundleDetailsActivity
import com.infinum.sentinel.ui.certificates.observer.CertificatesObserver
import com.infinum.sentinel.ui.certificates.observer.SentinelWorkManager
import com.infinum.sentinel.ui.crash.anr.SentinelAnrObserver
import com.infinum.sentinel.ui.crash.anr.SentinelAnrObserverRunnable
import com.infinum.sentinel.ui.crash.anr.SentinelUiAnrObserver
import com.infinum.sentinel.ui.crash.handler.SentinelExceptionHandler
import com.infinum.sentinel.ui.crash.handler.SentinelUncaughtExceptionHandler
import com.infinum.sentinel.ui.shared.Constants
import com.infinum.sentinel.ui.shared.notification.IntentFactory
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import com.infinum.sentinel.ui.shared.notification.NotificationIntentFactory
import com.infinum.sentinel.ui.shared.notification.SystemNotificationFactory
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Suppress("TooManyFunctions")
@Component
@DomainScope
internal abstract class DomainComponent(
    private val context: Context,
    @Component val dataComponent: DataComponent
) {

    private val scope = MainScope()

    abstract val intentFactory: IntentFactory

    abstract val notificationFactory: NotificationFactory

    abstract val sentinelExceptionHandler: SentinelExceptionHandler

    abstract val certificatesObserver: CertificatesObserver

    abstract val sentinelAnrObserver: SentinelAnrObserver

    abstract val sentinelWorkManager: SentinelWorkManager

    abstract val sentinelAnrObserverRunnable: SentinelAnrObserverRunnable

    abstract val executorService: ExecutorService

    abstract val collectors: Factories.Collector

    abstract val formatters: Factories.Formatter

    abstract val triggers: Repositories.Triggers

    abstract val formats: Repositories.Formats

    abstract val bundleMonitor: Repositories.BundleMonitor

    abstract val bundles: Repositories.Bundles

    abstract val preferences: Repositories.Preference

    abstract val crashMonitor: Repositories.CrashMonitor

    abstract val certificates: Repositories.Certificate

    abstract val certificateMonitor: Repositories.CertificateMonitor

    fun setup() {
        initializeCrashMonitor()
        initializeBundleMonitor()
        initializeCertificateMonitor()
        initializeTriggers()
    }

    @Provides
    fun executorService(): ExecutorService =
        Executors.newSingleThreadExecutor()

    @Provides
    fun intentFactory(): IntentFactory =
        NotificationIntentFactory(context)

    @Provides
    fun notificationFactory(): NotificationFactory =
        SystemNotificationFactory(context, intentFactory)

    @Provides
    @DomainScope
    fun sentinelExceptionHandler(
        dao: CrashesDao
    ): SentinelExceptionHandler =
        SentinelUncaughtExceptionHandler(context, notificationFactory, dao)

    @Provides
    @DomainScope
    fun certificatesObserver(
        collectors: Factories.Collector,
    ): CertificatesObserver =
        CertificatesObserver(context, collectors, notificationFactory)

    @Provides
    @DomainScope
    fun sentinelAnrObserverRunnable(dao: CrashesDao): SentinelAnrObserverRunnable =
        SentinelAnrObserverRunnable(context, notificationFactory, dao)

    @Provides
    @DomainScope
    fun sentinelWorkManager(): SentinelWorkManager =
        SentinelWorkManager(context)

    @Provides
    @DomainScope
    fun sentinelAnrObserver(): SentinelAnrObserver =
        SentinelUiAnrObserver(sentinelAnrObserverRunnable, executorService)

    @Provides
    @DomainScope
    fun formatters(
        plain: Formatters.Plain,
        markdown: Formatters.Markdown,
        json: Formatters.Json,
        xml: Formatters.Xml,
        html: Formatters.Html
    ): Factories.Formatter =
        FormatterFactory(plain, markdown, json, xml, html)

    @Suppress("LongParameterList")
    @Provides
    @DomainScope
    fun collectors(
        device: Collectors.Device,
        application: Collectors.Application,
        permissions: Collectors.Permissions,
        preferences: Collectors.Preferences,
        targetedPreferences: Collectors.TargetedPreferences,
        certificates: Collectors.Certificates,
        tools: Collectors.Tools
    ): Factories.Collector =
        CollectorFactory(device, application, permissions, preferences, targetedPreferences, certificates, tools)

    @Provides
    @DomainScope
    fun triggers(dao: TriggersDao, cache: TriggersCache): Repositories.Triggers =
        TriggersRepository(dao, cache)

    @Provides
    @DomainScope
    fun formats(dao: FormatsDao): Repositories.Formats =
        FormatsRepository(dao)

    @Provides
    @DomainScope
    fun bundleMonitor(dao: BundleMonitorDao): Repositories.BundleMonitor =
        BundleMonitorRepository(dao)

    @Provides
    @DomainScope
    fun bundles(cache: BundlesCache): Repositories.Bundles =
        BundlesRepository(cache)

    @Provides
    @DomainScope
    fun preferences(cache: PreferenceCache): Repositories.Preference =
        PreferenceRepository(context, cache)

    @Provides
    @DomainScope
    fun targetedPreferences(cache: TargetedPreferencesCache): Repositories.TargetedPreferences =
        TargetedPreferencesRepository(context, cache)

    @Provides
    @DomainScope
    fun crashMonitor(dao: CrashMonitorDao): Repositories.CrashMonitor =
        CrashMonitorRepository(dao)

    @Provides
    @DomainScope
    fun certificates(cache: CertificateCache): Repositories.Certificate =
        CertificateRepository(cache)

    @Provides
    @DomainScope
    fun certificateMonitor(dao: CertificateMonitorDao): Repositories.CertificateMonitor =
        CertificateMonitorRepository(dao)

    private fun initializeCrashMonitor() {
        Thread.setDefaultUncaughtExceptionHandler(sentinelExceptionHandler as Thread.UncaughtExceptionHandler)
        scope.launch {
            withContext(Dispatchers.IO) {
                val entity = crashMonitor.load(CrashMonitorParameters()).first()
                if (entity.notifyExceptions) {
                    sentinelExceptionHandler.start()
                } else {
                    sentinelExceptionHandler.stop()
                }
                if (entity.notifyExceptions) {
                    sentinelAnrObserver.start()
                } else {
                    sentinelAnrObserver.stop()
                }
            }
        }
    }

    private fun initializeCertificateMonitor() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        scope.launch {
            withContext(Dispatchers.IO) {
                val monitorEntity = certificateMonitor.load(CertificateMonitorParameters()).first()
                monitorEntity.takeIf { it.runOnStart }?.let {
                    certificatesObserver.activate(it)
                } ?: certificatesObserver.deactivate()
                monitorEntity.takeIf { it.runInBackground }?.let {
                    sentinelWorkManager.startCertificatesCheck(it)
                } ?: sentinelWorkManager.stopCertificatesCheck()
            }
        }
    }

    private fun initializeBundleMonitor() {
        val notificationCallbacks = BundleMonitorNotificationCallbacks()

        (this.context as? Application)?.registerActivityLifecycleCallbacks(notificationCallbacks)
        (this.context as? Application)
            ?.registerActivityLifecycleCallbacks(
                BundleMonitorActivityCallbacks { activity, timestamp, className, callSite, bundle ->
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val sizeTree = bundle.sizeTree()
                            bundles.save(
                                BundleParameters(
                                    descriptor = BundleDescriptor(
                                        timestamp = timestamp,
                                        className = className,
                                        callSite = callSite,
                                        bundleTree = sizeTree
                                    )
                                )
                            )

                            val currentMonitor =
                                bundleMonitor.load(BundleMonitorParameters()).first()
                            if (currentMonitor.notify && sizeTree.size >
                                currentMonitor.limit * Constants.BYTE_MULTIPLIER
                            ) {
                                notificationCallbacks.currentActivity?.let {
                                    Snackbar.make(
                                        it.window.decorView,
                                        buildString {
                                            append(className)
                                            append(System.lineSeparator())
                                            append(
                                                Formatter.formatFileSize(
                                                    activity,
                                                    sizeTree.size.toLong()
                                                )
                                            )
                                        },
                                        Snackbar.LENGTH_LONG
                                    )
                                        .setAction(R.string.sentinel_show) { view ->
                                            view.context.startActivity(
                                                Intent(activity, BundleDetailsActivity::class.java)
                                                    .apply {
                                                        putExtra(
                                                            Constants.Keys.BUNDLE_ID,
                                                            sizeTree.id
                                                        )
                                                    }
                                            )
                                        }
                                        .show()
                                }
                            }
                        }
                    }
                }
            )
    }

    private fun initializeTriggers() {
        scope.launch {
            withContext(Dispatchers.IO) {
                triggers.load(TriggerParameters()).first()
                    .forEach { entity ->
                        when (entity.type) {
                            TriggerType.SHAKE ->
                                context.enableShakeTrigger()?.let {
                                    entity.enabled = it
                                }

                            TriggerType.FOREGROUND ->
                                context.enableForegroundTrigger()?.let {
                                    entity.enabled = it
                                }

                            TriggerType.PROXIMITY ->
                                context.enableProximityTrigger()?.let {
                                    entity.enabled = it
                                }

                            TriggerType.USB_CONNECTED ->
                                context.enableUsbConnectedTrigger()?.let {
                                    entity.enabled = it
                                }

                            TriggerType.AIRPLANE_MODE_ON ->
                                context.enableAirplaneModeOnTrigger()?.let {
                                    entity.enabled = it
                                }

                            else -> null
                        }?.let {
                            triggers.save(
                                TriggerParameters(
                                    entity = entity
                                )
                            )
                        }
                    }
            }
        }
    }
}
