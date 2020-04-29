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
import com.infinum.sentinel.ui.tools.DummyTool
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

        private lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun setupBeforeClass() {
            context =
                ApplicationProvider.getApplicationContext<SentinelTestApplication>().applicationContext
        }
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    @SmallTest
    fun sentinel_isMemoizedSingleton() {
        val instance1 = Sentinel.watch(context)
        val instance2 = Sentinel.watch(context)

        assertEquals(instance1, instance2)
    }

    @Test
    @SmallTest
    fun sentinel_isMemoizedSingleton_WithDifferentTools() {
        val instance1 = Sentinel.watch(context)
        val instance2 = Sentinel.watch(context, setOf(DummyTool()))

        assertEquals(instance1, instance2)
    }

    @Test
    @SmallTest
    fun sentinel_watch() {
        Sentinel.watch(context)

        intended(hasComponent(SentinelActivity::class.java.name), times(0))
    }

    @Test
    @SmallTest
    fun sentinel_show() {
        val instance = Sentinel.watch(context)

        instance.show()

        intended(hasComponent(ComponentName(context, SentinelActivity::class.java)), times(2))
    }

//    @Test
//    @SmallTest
//    @RequiresDevice
//    fun sentinel_showDevice() {
//        val instance = Sentinel.watch(context)
//
//        instance.show()
//
//        intended(hasComponent(ComponentName(context, SentinelActivity::class.java)), times(1))
//    }
}
