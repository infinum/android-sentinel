package com.infinum.designer.ui.listeners

import android.animation.Animator

class NodeAnimatorListener(
    private val onStart: () -> Unit,
    private val onEnd: () -> Unit
) : Animator.AnimatorListener {
    
    override fun onAnimationStart(animation: Animator?) = onStart()

    override fun onAnimationEnd(animation: Animator?) = onEnd()

    override fun onAnimationRepeat(animation: Animator?) = Unit

    override fun onAnimationCancel(animation: Animator?) = Unit
}