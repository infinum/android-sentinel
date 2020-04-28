package com.infinum.sentinel.ui

import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.infinum.sentinel.R
import com.infinum.sentinel.di.SentinelComponent
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin


class FragmentTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val window: Window = window
        window.addFlags(
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )

        val content = FrameLayout(this)
        content.layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        content.id = R.id.fragmentContainer

        setContentView(content)

        startKoin {
            androidLogger()
            androidContext(this@FragmentTestActivity)
            modules(SentinelComponent.modules(setOf()) { })
        }
    }

    override fun onDestroy() {
        stopKoin()
        super.onDestroy()
    }

    fun setFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .addToBackStack(null)
            .commitAllowingStateLoss()
    }
}