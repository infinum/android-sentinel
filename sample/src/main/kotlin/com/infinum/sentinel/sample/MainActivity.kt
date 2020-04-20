package com.infinum.sentinel.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.sample.databinding.ActivityMainBinding
import com.infinum.sentinel.sample.tools.SentinelTools

class MainActivity : AppCompatActivity() {

    private var sentinel: Sentinel? = null

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        sentinel = Sentinel.watch(this, SentinelTools.get())

        viewBinding.button.setOnClickListener {
            sentinel?.show()
        }
    }
}
