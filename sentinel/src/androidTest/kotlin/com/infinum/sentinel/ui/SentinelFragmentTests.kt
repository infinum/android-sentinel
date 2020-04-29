package com.infinum.sentinel.ui

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SentinelFragmentTests : BaseKoinTest() {

    @Test
    fun sentinelFragment_show() {
        launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun sentinelFragment_showChild_Settings() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        scenario.onFragment {
            it.settings()

            assertEquals(
                it.viewBinding.toolbar.subtitle,
                context.getString(R.string.sentinel_settings)
            )
        }
    }
//
//    @Test
//    fun sentinelFragment_showChild_Application() {
//        startKoin {
//            androidLogger()
//            androidContext(context.applicationContext)
//            modules(SentinelComponent.modules(TOOLS_EMPTY) { })
//        }
//
//        val scenario =
//            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)
//
//        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
//        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
//        onView(withId(R.id.fab)).check(matches(isDisplayed()))
//        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
//        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
//        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
//
//        scenario.onFragment {
//            it.application()
//
//            assertEquals(
//                it.viewBinding.toolbar.subtitle,
//                context.getString(R.string.sentinel_application)
//            )
//        }
//
//        stopKoin()
//    }
}
