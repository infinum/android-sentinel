package com.infinum.sentinel.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding

class SentinelItemTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val viewBinding =
        SentinelViewItemTextBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    var data: String
        get() = viewBinding.valueView.text.toString()
        set(value) {
            viewBinding.valueView.text = value
        }

    init {
        parseAttributes(attrs)
    }

    private fun parseAttributes(attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.SentinelItemTextView) {
            getString(R.styleable.SentinelItemTextView_sentinel_label)?.let {
                viewBinding.labelView.text = it
            }
        }
    }
}
