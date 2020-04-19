package com.infinum.sentinel.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.sample.databinding.ActivityMainBinding
import com.infinum.sentinel.ui.tools.GooglePlayTool

class MainActivity : AppCompatActivity() {

    private var sentinel: Sentinel? = null

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        sentinel = Sentinel(
            this,
            listOf(ChuckTool(), CollarTool(), DbInspectorTool(), GooglePlayTool())
        )

        viewBinding.button.setOnClickListener {
            sentinel?.show()
        }
    }
}