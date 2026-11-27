package com.infinum.sentinel.domain.deeplinks

import com.infinum.sentinel.data.models.raw.deeplinks.DeepLinksData
import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData

internal interface DeepLinksCollectors {
    fun interface DeepLinks {
        operator fun invoke(): DeepLinksData
    }

    fun interface DomainVerification {
        operator fun invoke(): DomainVerificationData?
    }

    /** Whether the app enabled or disabled one of its components at runtime. */
    fun interface ComponentStates {
        /** True or false when the app set [componentName]'s state at runtime, null when the manifest decides. */
        fun runtimeEnabled(componentName: String): Boolean?
    }
}
