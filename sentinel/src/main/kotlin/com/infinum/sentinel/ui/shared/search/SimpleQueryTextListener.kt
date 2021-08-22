package com.infinum.sentinel.ui.shared.search

import androidx.appcompat.widget.SearchView

internal class SimpleQueryTextListener(
    private val onQueryTextChanged: (String?) -> Unit
) : SearchView.OnQueryTextListener {

    override fun onQueryTextSubmit(query: String?): Boolean {
        onQueryTextChanged(query)
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        onQueryTextChanged(newText)
        return true
    }
}
