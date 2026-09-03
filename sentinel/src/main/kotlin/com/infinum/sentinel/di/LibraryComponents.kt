package com.infinum.sentinel.di

import android.annotation.SuppressLint
import android.content.Context
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.di.component.DataComponent
import com.infinum.sentinel.di.component.DomainComponent
import com.infinum.sentinel.di.component.PresentationComponent
import com.infinum.sentinel.di.component.ViewModelComponent
import com.infinum.sentinel.di.component.create
import com.infinum.sentinel.ui.tools.AnnotationsTool
import com.infinum.sentinel.ui.tools.AppInfoTool
import com.infinum.sentinel.ui.tools.BundleMonitorTool
import com.infinum.sentinel.ui.tools.CertificateTool
import com.infinum.sentinel.ui.tools.CrashMonitorTool

/**
 * The tools Sentinel shows: the defaults, with any default replaced by a user-supplied tool of
 * the same type. This lets a configurable tool such as [com.infinum.sentinel.ui.tools.AnnotationsTool]
 * ship enabled by default while still accepting configuration through [Sentinel.watch].
 */
internal fun resolveTools(
    userTools: Set<Sentinel.Tool>,
    defaults: Set<Sentinel.Tool>,
): Set<Sentinel.Tool> {
    val overriddenTypes = userTools.map { it::class }.toSet()
    return userTools + defaults.filterNot { it::class in overriddenTypes }
}

@SuppressLint("StaticFieldLeak")
internal object LibraryComponents {
    internal const val DATABASE_VERSION = 5

    private val DEFAULT_TOOLS =
        setOf(
            CrashMonitorTool(),
            BundleMonitorTool(),
            AppInfoTool(),
            AnnotationsTool(),
        )

    @Suppress("LateinitUsage")
    private lateinit var dataComponent: DataComponent

    @Suppress("LateinitUsage")
    private lateinit var domainComponent: DomainComponent

    @Suppress("LateinitUsage")
    private lateinit var viewModelComponent: ViewModelComponent

    @Suppress("LateinitUsage")
    private lateinit var presentationComponent: PresentationComponent

    fun initialize(context: Context) {
        dataComponent = DataComponent::class.create(context)
        domainComponent = DomainComponent::class.create(context, dataComponent)
        viewModelComponent = ViewModelComponent::class.create(domainComponent)
        presentationComponent = PresentationComponent::class.create(context, viewModelComponent)
    }

    fun setup(
        tools: Set<Sentinel.Tool>,
        targetedPreferences: Map<String, List<String>>,
        onTriggered: () -> Unit,
    ) {
        dataComponent.setup(
            resolveTools(tools, DEFAULT_TOOLS),
            targetedPreferences,
            tools
                .filterIsInstance<CertificateTool>()
                .firstOrNull()
                ?.userCertificates
                .orEmpty(),
            onTriggered,
        )
        WorkManagerInitializer.init(domainComponent)
        domainComponent.setup()
    }

    fun presentation(): PresentationComponent = presentationComponent

    fun viewModels(): ViewModelComponent = viewModelComponent
}
