package com.infinum.designer.ui.models.configuration

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf

abstract class AbstractConfiguration : Configuration, Parcelable {

    abstract val enabled: Boolean

    override fun toBundle(): Bundle =
        bundleOf("configuration" to this@AbstractConfiguration)
}