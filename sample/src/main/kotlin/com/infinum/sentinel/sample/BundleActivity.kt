package com.infinum.sentinel.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.sentinel.sample.databinding.ActivityBundleBinding

class BundleActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivityBundleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivityBundleBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }
}