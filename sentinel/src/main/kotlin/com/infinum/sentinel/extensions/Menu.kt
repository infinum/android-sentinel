package com.infinum.sentinel.extensions

import android.view.Menu
import androidx.appcompat.widget.SearchView
import com.infinum.sentinel.R

internal val Menu.searchView
    get() = findItem(R.id.search)?.actionView as? SearchView
