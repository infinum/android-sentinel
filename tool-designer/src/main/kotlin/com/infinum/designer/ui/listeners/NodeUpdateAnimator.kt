package com.infinum.designer.ui.listeners

import android.animation.ValueAnimator

class NodeUpdateAnimator(
    private val onFractionUpdate: (Float) -> Unit
) : ValueAnimator.AnimatorUpdateListener {

    override fun onAnimationUpdate(animation: ValueAnimator?) =
        animation?.let {
            onFractionUpdate(it.animatedFraction)
        } ?: Unit
}