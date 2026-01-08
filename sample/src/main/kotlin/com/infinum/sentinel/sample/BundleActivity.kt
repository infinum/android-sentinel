package com.infinum.sentinel.sample

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.infinum.sentinel.sample.databinding.ActivityBundleBinding

class BundleActivity : AppCompatActivity() {
    @Suppress("LateinitUsage")
    private lateinit var viewBinding: ActivityBundleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewBinding = ActivityBundleBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        intent.extras?.let {
            viewBinding.bundleContent.text = it.toString()
        }
    }
}
