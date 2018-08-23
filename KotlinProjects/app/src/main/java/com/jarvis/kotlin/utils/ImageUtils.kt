package com.jarvis.kotlin.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.support.annotation.DrawableRes
import com.jarvis.kotlin.R

class ImageUtils {
    companion object {
        fun getDrawable(context: Context, @DrawableRes resId: Int): Drawable {
            val gradientDrawable: GradientDrawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return context.resources.getDrawable(R.drawable.divider_gradient, context.resources.newTheme())
            } else {
                return context.resources.getDrawable(R.drawable.divider_gradient) as GradientDrawable
            }
        }
    }
}
