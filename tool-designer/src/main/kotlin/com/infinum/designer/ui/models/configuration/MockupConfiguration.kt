package com.infinum.designer.ui.models.configuration

import kotlinx.android.parcel.Parcelize

@Parcelize
data class MockupConfiguration(
    override var enabled: Boolean = false,
    val opacity: Float = 0.2f,
    val portraitUri: String? = null,
    val landscapeUri: String? = null
) : AbstractConfiguration()