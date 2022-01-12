package com.infinum.sentinel.domain

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.Data
import com.infinum.sentinel.domain.bundle.descriptor.BundlesRepository
import com.infinum.sentinel.domain.bundle.monitor.BundleMonitorRepository
import com.infinum.sentinel.domain.certificate.CertificateRepository
import com.infinum.sentinel.domain.certificate.monitor.CertificateMonitorRepository
import com.infinum.sentinel.domain.collectors.CollectorFactory
import com.infinum.sentinel.domain.crash.monitor.CrashMonitorRepository
import com.infinum.sentinel.domain.formats.FormatsRepository
import com.infinum.sentinel.domain.formatters.FormatterFactory
import com.infinum.sentinel.domain.preference.PreferenceRepository
import com.infinum.sentinel.domain.triggers.TriggersRepository
import java.security.cert.X509Certificate
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

@Suppress("TooManyFunctions")
internal object Domain {

    private var tools: Set<Sentinel.Tool> = setOf()
    private var userCertificates: List<X509Certificate> = listOf()

    fun setup(
        tools: Set<Sentinel.Tool>,
        userManagers: List<X509Certificate>,
        onTriggered: () -> Unit
    ) {
        this.tools = tools
        this.userCertificates = userManagers
        Data.setup(onTriggered)
    }

    fun modules(): List<Module> =
        Data.modules().plus(
            listOf(
                collectors(),
                formatters(),
                triggers(),
                formats(),
                bundleMonitor(),
                bundles(),
                preferences(),
                crashMonitor(),
                certificates(),
                certificateMonitor()
            )
        )

    private fun collectors() = module {
        single<Factories.Collector> {
            CollectorFactory(
                get(),
                get(),
                get(),
                get(),
                get { parametersOf(userCertificates) },
                get { parametersOf(tools) }
            )
        }
    }

    private fun formatters() = module {
        single<Factories.Formatter> { FormatterFactory(get(), get(), get(), get(), get()) }
    }

    private fun triggers() = module {
        single<Repositories.Triggers> { TriggersRepository(get(), get()) }
    }

    private fun formats() = module {
        single<Repositories.Formats> { FormatsRepository(get()) }
    }

    private fun bundleMonitor() = module {
        single<Repositories.BundleMonitor> { BundleMonitorRepository(get()) }
    }

    private fun bundles() = module {
        single<Repositories.Bundles> { BundlesRepository(get()) }
    }

    private fun preferences() = module {
        single<Repositories.Preference> { PreferenceRepository(get(), get()) }
    }

    private fun crashMonitor() = module {
        single<Repositories.CrashMonitor> { CrashMonitorRepository(get()) }
    }

    private fun certificates() = module {
        single<Repositories.Certificate> { CertificateRepository(get()) }
    }

    private fun certificateMonitor() = module {
        single<Repositories.CertificateMonitor> { CertificateMonitorRepository(get()) }
    }
}
