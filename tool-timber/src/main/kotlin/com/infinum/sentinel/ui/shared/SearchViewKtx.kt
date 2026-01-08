package com.infinum.sentinel.ui.shared

import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import com.infinum.sentinel.R

internal fun SearchView.setup(
    hint: String?,
    onSearchClosed: () -> Unit,
    onQueryTextChanged: (String?) -> Unit,
) {
    setIconifiedByDefault(true)
    isSubmitButtonEnabled = false
    isQueryRefinementEnabled = true
    maxWidth = Integer.MAX_VALUE
    queryHint = hint
    setOnCloseListener {
        onSearchClosed()
        false
    }
    setOnQueryTextListener(
        SimpleQueryTextListener(onQueryTextChanged),
    )
    findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        .setImageResource(R.drawable.sentinel_ic_clear_search)
}
