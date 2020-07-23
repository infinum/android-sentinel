package com.infinum.designer.ui.fragments

import android.content.Context
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.infinum.designer.ui.ServiceActivity
import com.infinum.designer.ui.models.configuration.Configuration

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class AbstractOverlayFragment<CONFIGURATION: Configuration>(
    @LayoutRes contentLayoutId: Int
) : Fragment(contentLayoutId) {

    protected var serviceActivity: ServiceActivity? = null

    abstract val binding: ViewBinding

    abstract fun toggleUi(enabled: Boolean)

    abstract fun configure(configuration: CONFIGURATION)

    protected fun showMessage(text: String) =
        view?.let { Snackbar.make(it, text, Snackbar.LENGTH_SHORT).show() }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is ServiceActivity) {
            serviceActivity = context
        }
    }

    override fun onDetach() {
        serviceActivity = null
        super.onDetach()
    }
}
