package com.example.aspectratiocalculator

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.PathInterpolator
import com.google.android.material.R as MaterialR
import com.google.android.material.color.MaterialColors
import com.google.android.material.motion.MotionUtils
import com.google.android.material.textfield.TextInputLayout

/** Shared Material 3 motion and color helpers for both screens. */
object Motion {
    private fun emphasizedDecelerate(context: Context) = MotionUtils.resolveThemeInterpolator(
        context, MaterialR.attr.motionEasingEmphasizedDecelerateInterpolator, PathInterpolator(0.05f, 0.7f, 0.1f, 1f)
    )

    /** Fades and lifts [views] into place one after another. */
    fun enterStaggered(views: List<View>) {
        val first = views.firstOrNull() ?: return
        val distance = first.resources.displayMetrics.density * 24
        val interpolator = emphasizedDecelerate(first.context)
        views.forEachIndexed { i, view ->
            view.alpha = 0f
            view.translationY = distance
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(i * 60L)
                .setDuration(450)
                .setInterpolator(interpolator)
                .start()
        }
    }

    /** Runs [onFrame] with 0..1 so numbers can count up to their final value. */
    fun countUp(context: Context, onFrame: (Float) -> Unit): ValueAnimator =
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 600
            interpolator = emphasizedDecelerate(context)
            addUpdateListener { onFrame(it.animatedValue as Float) }
            start()
        }

    /** Small scale pop to draw attention to a freshly updated card. */
    fun pop(view: View) {
        view.scaleX = 0.96f
        view.scaleY = 0.96f
        view.animate().scaleX(1f).scaleY(1f).setDuration(350)
            .setInterpolator(emphasizedDecelerate(view.context)).start()
    }

    fun shake(view: View) {
        val d = view.resources.displayMetrics.density * 8
        ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -d, d, -d / 2, d / 2, 0f)
            .setDuration(350).start()
    }

    /** Primary-to-tertiary gradient so the preview follows the active (static or dynamic) scheme. */
    fun previewBackground(context: Context) = GradientDrawable(
        GradientDrawable.Orientation.TL_BR,
        intArrayOf(
            MaterialColors.getColor(context, MaterialR.attr.colorPrimary, 0),
            MaterialColors.getColor(context, MaterialR.attr.colorTertiary, 0)
        )
    ).apply { cornerRadius = context.resources.displayMetrics.density * 12 }

    fun previewTextColor(context: Context) = MaterialColors.getColor(context, MaterialR.attr.colorOnPrimary, 0)
}

fun TextInputLayout.fail(message: CharSequence) {
    error = message
    Motion.shake(this)
}
