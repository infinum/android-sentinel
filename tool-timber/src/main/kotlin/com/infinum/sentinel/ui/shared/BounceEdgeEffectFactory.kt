package com.infinum.sentinel.ui.shared

import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView

internal class BounceEdgeEffectFactory : RecyclerView.EdgeEffectFactory() {

    override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect =
        BounceEdgeEffect(recyclerView, direction)
}
