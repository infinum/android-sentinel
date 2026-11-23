package com.infinum.sentinel.ui.deeplinks

import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksOverviewAssembler
import com.infinum.sentinel.domain.deeplinks.variants.VariantsWriteException
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

@Inject
internal class DeepLinksViewModel(
    private val repository: Repositories.DeepLinks,
    private val variants: Repositories.DeepLinkVariants,
) : BaseChildViewModel<DeepLinksState, DeepLinksEvent>() {
    /** One read of the App Links verification state. [data] is null when it can't be read (FR-44). */
    private data class VerificationRead(
        val data: DomainVerificationData?,
    )

    private var isStarted = false

    /**
     * The latest verification read, null until the first read finishes. The list waits for it, so it doesn't
     * show first without statuses and then again with them.
     */
    private val verification = MutableStateFlow<VerificationRead?>(null)

    /**
     * Reads the verification snapshot again, so statuses follow what the tester changed in system settings
     * (FR-43). It never runs discovery again.
     */
    fun refreshVerification() {
        launch { verification.value = VerificationRead(io { repository.verification() }) }
    }

    /** Saves a copy of the variant [id] named with [copySuffix]. The list updates through the observed variants. */
    fun duplicate(
        id: String,
        copySuffix: String,
    ) = change { variants.duplicate(id, copySuffix) }

    /** Deletes the variant [id]. */
    fun delete(id: String) = change { variants.delete(id) }

    /** Deletes every saved variant. */
    fun deleteAll() = change { variants.deleteAll() }

    private fun change(block: suspend () -> Unit) {
        launch {
            try {
                io { block() }
            } catch (ignored: VariantsWriteException) {
                emitEvent(DeepLinksEvent.ChangeFailed)
            }
        }
    }

    override fun data() {
        if (isStarted) return
        isStarted = true
        // The screen also refreshes on resume; this first read doesn't depend on it.
        if (verification.value == null) refreshVerification()
        launch {
            val states =
                combine(
                    flow { emit(repository.discover()) },
                    variants.observe(),
                    verification.filterNotNull(),
                ) { discovery, saved, read ->
                    DeepLinksState.Data(
                        overview = DeepLinksOverviewAssembler.assemble(discovery, saved, read.data),
                        variantCount = saved.size,
                        isLinkSettingsAvailable = read.data != null,
                    )
                }
            states
                .flowOn(runningDispatchers)
                .collect { setState(it) }
        }
    }
}
