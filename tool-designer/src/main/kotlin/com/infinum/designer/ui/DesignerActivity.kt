package com.infinum.designer.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RestrictTo
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerActivityDesignerBinding
import com.infinum.designer.ui.fragments.GridOverlayFragment
import com.infinum.designer.ui.fragments.MagnifierOverlayFragment
import com.infinum.designer.ui.fragments.MockupOverlayFragment
import com.infinum.designer.ui.models.PermissionRequest
import com.infinum.designer.ui.models.configuration.DesignerConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DesignerActivity : ServiceActivity() {

    private lateinit var binding: DesignerActivityDesignerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DesignerActivityDesignerBinding.inflate(layoutInflater)
            .also { setContentView(it.root) }
            .also { binding = it }
            .also {
                setupToolbar()
                setupUi(DesignerConfiguration())
                setupPermission()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        PermissionRequest(requestCode)
            ?.let { permission ->
                when (permission) {
                    PermissionRequest.OVERLAY -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (Settings.canDrawOverlays(this).not()) {
                                showMessage("Overlay permission denied")
                            } else {
                                Unit
                            }
                        } else {
                            Unit
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }
    }

    override fun setupUi(configuration: DesignerConfiguration) {
        (binding.toolbar.menu.findItem(R.id.status).actionView as? SwitchMaterial)?.let {
            it.setOnCheckedChangeListener(null)
            it.isChecked = configuration.enabled
            it.setOnCheckedChangeListener { _, isChecked ->
                when (isChecked) {
                    true -> createService()
                    false -> destroyService()
                }
            }
        }
        with(supportFragmentManager) {
            (findFragmentById(R.id.gridOverlayFragment) as? GridOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.grid)
            }
            (findFragmentById(R.id.mockupOverlayFragment) as? MockupOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.mockup)
            }
            (findFragmentById(R.id.magnifierOverlayFragment) as? MagnifierOverlayFragment)?.let {
                it.toggleUi(configuration.enabled)
                it.configure(configuration.magnifier)
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(this).not()) {
                startActivityForResult(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    ),
                    PermissionRequest.OVERLAY.requestCode
                )
            } else {
                Unit
            }
        } else {
            Unit
        }
    }

    private fun showMessage(text: String) =
        Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
}