package com.infinum.sentinel.ui.deeplinks.breakdown

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.Constants
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class LinkBreakdownActivity : BaseChildActivity() {
    companion object {
        /** Creates the intent that opens the breakdown of [link]. */
        fun newIntent(
            context: Context,
            link: String,
        ): Intent =
            Intent(context, LinkBreakdownActivity::class.java)
                .putExtra(Constants.Keys.DEEP_LINK, link)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    android.R.id.content,
                    LinkBreakdownFragment.newInstance(intent.getStringExtra(Constants.Keys.DEEP_LINK).orEmpty()),
                    LinkBreakdownFragment.TAG,
                ).commit()
        }
    }
}
