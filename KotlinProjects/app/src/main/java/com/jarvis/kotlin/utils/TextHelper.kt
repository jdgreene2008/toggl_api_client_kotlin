package com.jarvis.kotlin.utils

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan

class TextHelper {


    companion object {
        fun applyBoldSpan(text: String?, startOffset: Int): SpannableString? {
            return applySpan(text, startOffset, null, StyleSpan(Typeface.BOLD))
        }

        fun applyBoldSpan(text: String?, startOffset: Int, endOffset: Int?): SpannableString? {
            return applySpan(text, startOffset, endOffset, StyleSpan(Typeface.BOLD))
        }

        fun applySpan(text: String, startOffset: Int, span: MetricAffectingSpan): SpannableString? {
            return applySpan(text, startOffset, null, span)
        }

        fun applySpan(text: String?, startOffset: Int, endOffset: Int?, span: MetricAffectingSpan): SpannableString? {
            if (TextUtils.isEmpty(text)) {
                return null
            }
            val spannable = SpannableString(text)
            if (startOffset < 0 || startOffset >= text!!.length) {
                throw IndexOutOfBoundsException("applySpan, startOffset is: " + startOffset + ", text length is " +
                        text!!.length)
            }
            if (endOffset == null || endOffset < startOffset || endOffset > text.length) {
                spannable.setSpan(span, startOffset, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                spannable.setSpan(span, startOffset, endOffset, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            return spannable
        }


    }


}
