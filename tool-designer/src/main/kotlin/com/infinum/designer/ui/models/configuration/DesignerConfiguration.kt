package com.infinum.designer.ui.models.configuration

import kotlinx.android.parcel.Parcelize

@Parcelize
data class DesignerConfiguration(
    override val enabled: Boolean = false,
    val grid: GridConfiguration = GridConfiguration(),
    val mockup: MockupConfiguration = MockupConfiguration(),
    val magnifier: MagnifierConfiguration = MagnifierConfiguration()
) : AbstractConfiguration()