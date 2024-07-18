package com.infinum.sentinel.di

import android.annotation.SuppressLint
import android.content.Context
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.di.component.DataComponent
import com.infinum.sentinel.di.component.DomainComponent
import com.infinum.sentinel.di.component.PresentationComponent
import com.infinum.sentinel.di.component.ViewModelComponent
import com.infinum.sentinel.di.component.create
import com.infinum.sentinel.ui.tools.AppInfoTool
import com.infinum.sentinel.ui.tools.BundleMonitorTool
import com.infinum.sentinel.ui.tools.CertificateTool
import com.infinum.sentinel.ui.tools.CrashMonitorTool

@SuppressLint("StaticFieldLeak")
internal object LibraryComponents {

    internal const val DATABASE_VERSION = 5

    private val DEFAULT_TOOLS = setOf(
        CrashMonitorTool(),
        BundleMonitorTool(),
        AppInfoTool()
    )

    private lateinit var dataComponent: DataComponent
    private lateinit var domainComponent: DomainComponent
    private lateinit var viewModelComponent: ViewModelComponent
    private lateinit var presentationComponent: PresentationComponent

    fun initialize(context: Context) {
        dataComponent = DataComponent::class.create(context)
        domainComponent = DomainComponent::class.create(context, dataComponent)
        viewModelComponent = ViewModelComponent::class.create(domainComponent)
        presentationComponent = PresentationComponent::class.create(context, viewModelComponent)
    }

    fun setup(tools: Set<Sentinel.Tool>, onTriggered: () -> Unit) {
        dataComponent.setup(
            tools.plus(DEFAULT_TOOLS),
            tools.filterIsInstance<CertificateTool>().firstOrNull()?.userCertificates.orEmpty(),
            onTriggered
        )
        WorkManagerInitializer.init(domainComponent)
        domainComponent.setup()
    }

    fun presentation(): PresentationComponent = presentationComponent

    fun viewModels(): ViewModelComponent = viewModelComponent
}
