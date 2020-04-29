package com.infinum.sentinel.ui

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.widget.ActionMenuView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.appbar.MaterialToolbar
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.Domain
import com.infinum.sentinel.ui.children.ApplicationFragment
import com.infinum.sentinel.ui.children.DeviceFragment
import com.infinum.sentinel.ui.children.PermissionsFragment
import com.infinum.sentinel.ui.children.PreferencesFragment
import com.infinum.sentinel.ui.children.SettingsFragment
import com.infinum.sentinel.ui.children.ToolsFragment
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class SentinelFragmentTests {

    companion object {

        private lateinit var context: Context

        @BeforeClass
        @JvmStatic
        fun setupBeforeClass() {
            context =
                ApplicationProvider.getApplicationContext<SentinelTestApplication>().applicationContext

            Domain.initialise(context, setOf()) {}
        }
    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun sentinelFragment_show() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)
        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(ToolsFragment.TAG)
            assertNotNull(childFragment)

            assertEquals(
                it.viewBinding.toolbar.subtitle,
                context.getString(R.string.sentinel_tools)
            )
        }

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.share)).check(matches(isDisplayed()))
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

        onView(withId(R.id.settings)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.settings),
                withParent(instanceOf(ActionMenuView::class.java))
            )
        ).perform(click())

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withText(R.string.sentinel_settings)).check(matches(withParent(withId(R.id.toolbar))))

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(SettingsFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    fun sentinelFragment_showChild_Device() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.device),
                withParent(instanceOf(ActionMenuView::class.java))
            )
        ).perform(click())

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(R.string.sentinel_device),
                withParent(instanceOf(MaterialToolbar::class.java))
            )
        ).check(matches(withParent(withId(R.id.toolbar))))

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(DeviceFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    fun sentinelFragment_showChild_Application() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.application),
                withParent(instanceOf(ActionMenuView::class.java))
            )
        ).perform(click())

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(R.string.sentinel_application),
                withParent(instanceOf(MaterialToolbar::class.java))
            )
        ).check(matches(withParent(withId(R.id.toolbar))))

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(ApplicationFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    fun sentinelFragment_showChild_Permissions() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.permissions),
                withParent(instanceOf(ActionMenuView::class.java))
            )
        ).perform(click())

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(R.string.sentinel_permissions),
                withParent(instanceOf(MaterialToolbar::class.java))
            )
        ).check(matches(withParent(withId(R.id.toolbar))))

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(PermissionsFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    fun sentinelFragment_showChild_Preferences() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.preferences),
                withParent(instanceOf(ActionMenuView::class.java))
            )
        ).perform(lenientClick())

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(R.string.sentinel_preferences),
                withParent(instanceOf(MaterialToolbar::class.java))
            )
        ).check(matches(withParent(withId(R.id.toolbar))))

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(PreferencesFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    fun sentinelFragment_showChild_Tools() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.fab)
            )
        ).perform(click())

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(
            allOf(
                withText(R.string.sentinel_tools),
                withParent(instanceOf(MaterialToolbar::class.java))
            )
        ).check(matches(withParent(withId(R.id.toolbar))))

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(ToolsFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    fun sentinelFragment_share() {

        val scenario =
            launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_BottomSheet)
        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(ToolsFragment.TAG)
            assertNotNull(childFragment)

            assertEquals(
                it.viewBinding.toolbar.subtitle,
                context.getString(R.string.sentinel_tools)
            )
        }

        onView(withId(R.id.sentinelTitle)).check(matches(withText(R.string.sentinel_name)))
        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomAppBar)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.share)).check(matches(isDisplayed()))

        Intents.init()

        onView(allOf(withId(R.id.share))).perform(click())

        Intents.intended(allOf(hasAction(Intent.ACTION_CHOOSER)), Intents.times(1))

        Intents.release()
    }

    private fun lenientClick(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isEnabled()
            }

            override fun getDescription(): String {
                return "Barely clicking"
            }

            override fun perform(uiController: UiController?, view: View) {
                view.performClick()
            }
        }
    }
}
