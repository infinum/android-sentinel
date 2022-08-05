package com.infinum.sentinel.ui.shared.span

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.extensions.allOccurrenceIndexes
import java.text.Normalizer

internal inline fun TextView.setSpan(
    bufferType: TextView.BufferType? = null,
    spanBuilder: Builder.() -> Unit
) {
    val builder = Builder(context)
    builder.spanBuilder()
    if (bufferType != null) {
        setText(builder.build(), bufferType)
    } else {
        text = builder.build()
    }
}

@Suppress("TooManyFunctions")
internal class Builder(
    private val context: Context,
    @FontRes private val fontBold: Int? = null,
    @FontRes private val fontSemiBold: Int? = null,
    @FontRes private val fontRegular: Int? = null,
    @FontRes private val fontLight: Int? = null
) {
    private val spanBuilder = SpannableStringBuilder()

    fun append(text: String) {
        append(SpannableString(text))
    }

    fun append(span: Spannable) {
        spanBuilder.append(span)
    }

    fun append(span: CharSequence) {
        spanBuilder.append(span)
    }

    fun span(charSequence: CharSequence, what: Any) =
        when (charSequence) {
            is String -> SpannableString(charSequence).apply {
                setSpan(what, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            is SpannableStringBuilder -> charSequence.apply {
                setSpan(what, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            is SpannableString -> charSequence.apply {
                setSpan(what, 0, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            else -> throw IllegalArgumentException("Unsuppported object type $what")
        }

    fun bold(span: CharSequence): CharSequence =
        span(span, CustomTypefaceSpan(ResourcesCompat.getFont(context, fontBold!!)!!))

    fun semiBold(span: CharSequence): CharSequence =
        span(span, CustomTypefaceSpan(ResourcesCompat.getFont(context, fontSemiBold!!)!!))

    fun regular(span: CharSequence): CharSequence =
        span(span, CustomTypefaceSpan(ResourcesCompat.getFont(context, fontRegular!!)!!))

    fun light(span: CharSequence): CharSequence =
        span(span, CustomTypefaceSpan(ResourcesCompat.getFont(context, fontLight!!)!!))

    fun italic(span: CharSequence): CharSequence =
        span(span, StyleSpan(Typeface.ITALIC))

    fun underline(span: CharSequence): CharSequence =
        span(span, UnderlineSpan())

    fun clickable(span: CharSequence, listener: ClickableSpan): CharSequence =
        span(span, listener)

    fun fontSize(span: CharSequence, sizeInSp: Int): CharSequence =
        span(span, AbsoluteSizeSpan(sizeInSp, true))

    fun fontColor(span: CharSequence, color: Int): CharSequence =
        span(span, ForegroundColorSpan(color))

    fun highlight(span: CharSequence, search: String?): CharSequence {
        if (search.isNullOrEmpty()) {
            return span
        } else {
            val normalizedText = Normalizer.normalize(span, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                .lowercase()

            val startIndexes = normalizedText.allOccurrenceIndexes(search)
            return if (startIndexes.isNotEmpty()) {
                val highlighted: Spannable = SpannableString(span)
                startIndexes.forEach {
                    highlighted.setSpan(
                        BackgroundColorSpan(ContextCompat.getColor(context, R.color.sentinel_primary)),
                        it,
                        it + search.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                highlighted
            } else {
                span
            }
        }
    }

    fun build(): CharSequence = spanBuilder

    inner class CustomTypefaceSpan(
        private val font: Typeface
    ) : MetricAffectingSpan() {

        override fun updateMeasureState(p: TextPaint) {
            update(p)
        }

        override fun updateDrawState(tp: TextPaint) {
            update(tp)
        }

        private fun update(textPaint: TextPaint) {
            textPaint.apply {
                val oldTypeface = typeface
                val oldStyle = oldTypeface?.style ?: 0

                val newTypeface = Typeface.create(font, oldStyle)

                typeface = newTypeface
            }
        }
    }
}
