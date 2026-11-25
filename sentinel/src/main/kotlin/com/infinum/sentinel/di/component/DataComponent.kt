package com.infinum.sentinel.di.component

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.models.memory.triggers.airplanemode.AirplaneModeOnTrigger
import com.infinum.sentinel.data.models.memory.triggers.foreground.ForegroundTrigger
import com.infinum.sentinel.data.models.memory.triggers.manual.ManualTrigger
import com.infinum.sentinel.data.models.memory.triggers.proximity.ProximityTrigger
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.data.models.memory.triggers.usb.UsbConnectedTrigger
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase
import com.infinum.sentinel.data.sources.local.room.callbacks.SentinelDefaultValuesCallback
import com.infinum.sentinel.data.sources.local.room.dao.BundleMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CertificateMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CrashMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao
import com.infinum.sentinel.data.sources.local.room.dao.TriggersDao
import com.infinum.sentinel.data.sources.memory.bundles.BundlesCache
import com.infinum.sentinel.data.sources.memory.bundles.InMemoryBundlesCache
import com.infinum.sentinel.data.sources.memory.certificate.CertificateCache
import com.infinum.sentinel.data.sources.memory.certificate.InMemoryCertificateCache
import com.infinum.sentinel.data.sources.memory.preference.InMemoryPreferenceCache
import com.infinum.sentinel.data.sources.memory.preference.PreferenceCache
import com.infinum.sentinel.data.sources.memory.triggers.TriggersCache
import com.infinum.sentinel.data.sources.memory.triggers.TriggersCacheFactory
import com.infinum.sentinel.data.sources.raw.collectors.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.collectors.CertificateCollector
import com.infinum.sentinel.data.sources.raw.collectors.DeviceCollector
import com.infinum.sentinel.data.sources.raw.collectors.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.collectors.PreferencesCollector
import com.infinum.sentinel.data.sources.raw.collectors.ToolsCollector
import com.infinum.sentinel.data.sources.raw.formatters.HtmlFormatter
import com.infinum.sentinel.data.sources.raw.formatters.JsonFormatter
import com.infinum.sentinel.data.sources.raw.formatters.MarkdownFormatter
import com.infinum.sentinel.data.sources.raw.formatters.PlainFormatter
import com.infinum.sentinel.data.sources.raw.formatters.XmlFormatter
import com.infinum.sentinel.di.LibraryComponents
import com.infinum.sentinel.di.scope.DataScope
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.formatters.Formatters
import java.security.cert.X509Certificate
import java.util.Locale
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.Provides

@Suppress("TooManyFunctions")
@Component
@DataScope
internal abstract class DataComponent(
    private val context: Context,
) {
    private var tools: Set<Sentinel.Tool> = setOf()
    private var targetedPreferences: Map<String, List<String>> = mapOf()
    private var userCertificates: List<X509Certificate> = listOf()
    private var onTriggered: () -> Unit = {}

    fun setup(
        tools: Set<Sentinel.Tool>,
        targetedPreferences: Map<String, List<String>>,
        userCertificates: List<X509Certificate>,
        onTriggered: () -> Unit,
    ) {
        this.tools = tools
        this.targetedPreferences = targetedPreferences
        this.userCertificates = userCertificates
        this.onTriggered = onTriggered
    }

    //region Local
    abstract val database: SentinelDatabase

    abstract val callback: RoomDatabase.Callback

    abstract val triggersDao: TriggersDao

    abstract val formatsDao: FormatsDao

    abstract val bundleMonitorDao: BundleMonitorDao

    abstract val crashesDao: CrashesDao

    abstract val crashMonitorDao: CrashMonitorDao

    abstract val certificateMonitorDao: CertificateMonitorDao

    @Provides
    @DataScope
    fun callback(): RoomDatabase.Callback = SentinelDefaultValuesCallback()

    @Provides
    @Inject
    @DataScope
    fun database(): SentinelDatabase =
        Room
            .databaseBuilder(
                context,
                SentinelDatabase::class.java,
                String.format(
                    Locale.getDefault(),
                    "sentinel_%s_v%s.db",
                    context.applicationContext
                        .packageName
                        .replace(".", "_")
                        .lowercase(Locale.getDefault()),
                    LibraryComponents.DATABASE_VERSION,
                ),
            ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(callback)
            .build()

    @Provides
    @DataScope
    fun triggers(): TriggersDao = database.triggers()

    @Provides
    @DataScope
    fun formats(): FormatsDao = database.formats()

    @Provides
    @DataScope
    fun bundleMonitor(): BundleMonitorDao = database.bundleMonitor()

    @Provides
    @DataScope
    fun crashes(): CrashesDao = database.crashes()

    @Provides
    @DataScope
    fun crashMonitor(): CrashMonitorDao = database.crashMonitor()

    @Provides
    @DataScope
    fun certificateMonitor(): CertificateMonitorDao = database.certificateMonitor()

    //endregion

    //region Raw
    abstract val deviceCollector: Collectors.Device

    abstract val applicationCollector: Collectors.Application

    abstract val permissionsCollector: Collectors.Permissions

    abstract val preferencesCollector: Collectors.Preferences

    abstract val certificatesCollector: Collectors.Certificates

    abstract val toolsCollector: Collectors.Tools

    abstract val plainFormatter: Formatters.Plain

    abstract val markdownFormatter: Formatters.Markdown

    abstract val jsonFormatter: Formatters.Json

    abstract val xmlFormatter: Formatters.Xml

    abstract val htmlFormatter: Formatters.Html

    @Provides
    @DataScope
    fun deviceCollector(): Collectors.Device = DeviceCollector(context)

    @Provides
    @DataScope
    fun applicationCollector(): Collectors.Application = ApplicationCollector(context)

    @Provides
    @DataScope
    fun permissionsCollector(): Collectors.Permissions = PermissionsCollector(context)

    @Provides
    @DataScope
    fun preferencesCollector(): Collectors.Preferences = PreferencesCollector(context, targetedPreferences)

    @Provides
    @DataScope
    fun certificatesCollector(): Collectors.Certificates = CertificateCollector(userCertificates)

    @Provides
    @DataScope
    fun toolsCollector(): Collectors.Tools = ToolsCollector(tools)

    @Provides
    @DataScope
    fun plainFormatter(): Formatters.Plain =
        PlainFormatter(
            context,
            applicationCollector,
            permissionsCollector,
            deviceCollector,
            preferencesCollector,
        )

    @Provides
    @DataScope
    fun markdownFormatter(): Formatters.Markdown =
        MarkdownFormatter(
            context,
            applicationCollector,
            permissionsCollector,
            deviceCollector,
            preferencesCollector,
        )

    @Provides
    @DataScope
    fun jsonFormatter(): Formatters.Json =
        JsonFormatter(
            context,
            applicationCollector,
            permissionsCollector,
            deviceCollector,
            preferencesCollector,
        )

    @Provides
    @DataScope
    fun xmlFormatter(): Formatters.Xml =
        XmlFormatter(
            context,
            applicationCollector,
            permissionsCollector,
            deviceCollector,
            preferencesCollector,
        )

    @Provides
    @DataScope
    fun htmlFormatter(): Formatters.Html =
        HtmlFormatter(
            context,
            applicationCollector,
            permissionsCollector,
            deviceCollector,
            preferencesCollector,
        )
    //endregion

    //region Memory
    abstract val manualTrigger: ManualTrigger

    abstract val foregroundTrigger: ForegroundTrigger

    abstract val shakeTrigger: ShakeTrigger

    abstract val proximityTrigger: ProximityTrigger

    abstract val usbConnectedTrigger: UsbConnectedTrigger

    abstract val airplaneModeOnTrigger: AirplaneModeOnTrigger

    abstract val triggersCache: TriggersCache

    abstract val bundlesCache: BundlesCache

    abstract val preferenceCache: PreferenceCache

    abstract val certificateCache: CertificateCache

    @Provides
    @DataScope
    fun manualTrigger(): ManualTrigger = ManualTrigger()

    @Provides
    @DataScope
    fun shakeTrigger(): ShakeTrigger = ShakeTrigger(context, this.onTriggered)

    @Provides
    @DataScope
    fun foregroundTrigger(): ForegroundTrigger = ForegroundTrigger(this.onTriggered)

    @Provides
    @DataScope
    fun proximityTrigger(): ProximityTrigger = ProximityTrigger(context, this.onTriggered)

    @Provides
    @DataScope
    fun usbConnectedTrigger(): UsbConnectedTrigger = UsbConnectedTrigger(context, this.onTriggered)

    @Provides
    @DataScope
    fun airplaneModeOnTrigger(): AirplaneModeOnTrigger = AirplaneModeOnTrigger(context, this.onTriggered)

    @Provides
    @DataScope
    fun triggersCache(): TriggersCache =
        TriggersCacheFactory(
            manualTrigger,
            foregroundTrigger,
            shakeTrigger,
            proximityTrigger,
            usbConnectedTrigger,
            airplaneModeOnTrigger,
        )

    @Provides
    @DataScope
    fun bundlesCache(): BundlesCache = InMemoryBundlesCache()

    @Provides
    @DataScope
    fun preferenceCache(): PreferenceCache = InMemoryPreferenceCache()

    @Provides
    @DataScope
    fun certificateCache(): CertificateCache = InMemoryCertificateCache()
    //endregion
}
