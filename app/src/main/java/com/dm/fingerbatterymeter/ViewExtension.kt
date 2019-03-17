package com.dm.fingerbatterymeter

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

fun View.showBlinkAnimation(duration: Long) {
    val anim = AlphaAnimation(0.0f, 1.0f)
    anim.duration = duration
    anim.repeatMode = Animation.REVERSE
    anim.repeatCount = Animation.INFINITE
    startAnimation(anim)
}