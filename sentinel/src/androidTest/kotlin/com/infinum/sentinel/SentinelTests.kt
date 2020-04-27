package com.infinum.sentinel

import android.content.ComponentName
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.times
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.ui.SentinelActivity
import com.infinum.sentinel.ui.SentinelTestApplication
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
internal class SentinelTests {

    companion object {

        private val TOOLS_EMPTY = setOf<Sentinel.Tool>()

        lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            context = ApplicationProvider.getApplicationContext<SentinelTestApplication>()
                .applicationContext
        }
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun startIntentRecorder() {
        Intents.init()
    }

    @After
    fun stopIntentRecorder() {
        Intents.release()
    }

    @Test
    @SmallTest
    fun sentinel_isSingleton() {
        val instance1 = Sentinel.watch(context, TOOLS_EMPTY)
        val instance2 = Sentinel.watch(context, TOOLS_EMPTY)

        assertEquals(instance1, instance2)
    }

    @Test
    @SmallTest
    fun sentinel_Watch() {
        Sentinel.watch(context, TOOLS_EMPTY)

        intended(hasComponent(SentinelActivity::class.java.name), times(0))
    }

    @Test
    @SmallTest
    fun sentinel_Show() {
        val instance = Sentinel.watch(context, TOOLS_EMPTY)

        instance.show()
        
        intended(hasComponent(ComponentName(context, SentinelActivity::class.java)), times(3))
    }
}
