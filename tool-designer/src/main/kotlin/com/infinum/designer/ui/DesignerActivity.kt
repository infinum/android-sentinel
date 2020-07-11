package com.infinum.designer.ui

import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.fragment.app.FragmentActivity
import com.infinum.designer.databinding.DesignerActivityDesignerBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DesignerActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(DesignerActivityDesignerBinding.inflate(layoutInflater).root)
    }
}
