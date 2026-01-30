package com.infinum.sentinel.ui

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.infinum.sentinel.R
import com.infinum.sentinel.ui.main.SentinelFragment
import com.infinum.sentinel.ui.main.application.ApplicationFragment
import com.infinum.sentinel.ui.main.device.DeviceFragment
import com.infinum.sentinel.ui.main.permissions.PermissionsFragment
import com.infinum.sentinel.ui.main.preferences.PreferencesFragment
import com.infinum.sentinel.ui.main.tools.ToolsFragment
import com.infinum.sentinel.ui.settings.SettingsActivity
import com.infinum.sentinel.ui.settings.SettingsFragment
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
public class SentinelFragmentTests {
    public companion object {
        private lateinit var context: Context

        @BeforeClass
        @JvmStatic
        public fun setupBeforeClass() {
            context = ApplicationProvider.getApplicationContext<SentinelTestApplication>()
        }
    }

    @get:Rule
    public val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    public fun sentinelFragment_show() {
        val scenario = launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_Dialog)

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(ToolsFragment.TAG)
            assertNotNull(childFragment)
        }

        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.share)).check(matches(isDisplayed()))
    }

    @Test
    public fun sentinelFragment_launches_SettingsActivity() {
        launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_Dialog)

        Intents.init()

        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        // find navigationIcon
        onView(
            allOf(
                withContentDescription(R.string.sentinel_settings),
                isAssignableFrom(AppCompatImageButton::class.java),
            ),
        ).check(matches(isDisplayed()))

        // click navigationIcon
        onView(
            allOf(
                withContentDescription(R.string.sentinel_settings),
                isAssignableFrom(AppCompatImageButton::class.java),
            ),
        ).perform(click())

        Intents.intended(hasComponent(SettingsActivity::class.java.name))
        Intents.release()
    }

    @Test
    public fun settingsActivity_showSettingsFragment() {
        val scenario = ActivityScenario.launch(SettingsActivity::class.java)

        scenario.onActivity {
            val childFragment = it.supportFragmentManager.findFragmentByTag(SettingsFragment.TAG)

            assertNotNull(childFragment)
        }

        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withText(R.string.sentinel_settings)).check(matches(withParent(withId(R.id.toolbar))))
        onView(withId(R.id.triggersLayout)).check(matches(isDisplayed()))
        onView(withId(R.id.formatGroup)).check(matches(isDisplayed()))
        onView(withId(R.id.bundleMonitorLayout)).check(matches(isDisplayed()))
    }

    @Test
    public fun sentinelFragment_showChild_Device() {
        val scenario = launchFragmentInContainer<SentinelFragment>(themeResId = R.style.Sentinel_Theme_Dialog)

        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.device),
                isDescendantOfA(withId(R.id.bottomNavigation)),
            ),
        ).perform(click())

        onView(withId(R.id.manufacturerView)).check(matches(isDisplayed()))
    }

    @Test
    public fun sentinelFragment_showChild_Application() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(
                themeResId = R.style.Sentinel_Theme_Dialog,
            )

        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.share)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.application),
                isDescendantOfA(withId(R.id.bottomNavigation)),
            ),
        ).perform(click())

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(ApplicationFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    public fun sentinelFragment_showChild_Permissions() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(
                themeResId = R.style.Sentinel_Theme_Dialog,
            )

        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.permissions),
                isDescendantOfA(withId(R.id.bottomNavigation)),
            ),
        ).perform(click())

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(PermissionsFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    public fun sentinelFragment_showChild_Preferences() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(
                themeResId = R.style.Sentinel_Theme_Dialog,
            )

        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.preferences),
                isDescendantOfA(withId(R.id.bottomNavigation)),
            ),
        ).perform(lenientClick())

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(PreferencesFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    public fun sentinelFragment_showChild_Tools() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(
                themeResId = R.style.Sentinel_Theme_Dialog,
            )

        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))

        onView(
            allOf(
                withId(R.id.fab),
            ),
        ).perform(click())

        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(ToolsFragment.TAG)

            assertNotNull(childFragment)
        }
    }

    @Test
    public fun sentinelFragment_share() {
        val scenario =
            launchFragmentInContainer<SentinelFragment>(
                themeResId = R.style.Sentinel_Theme_Dialog,
            )
        scenario.onFragment {
            val childFragment = it.childFragmentManager.findFragmentByTag(ToolsFragment.TAG)
            assertNotNull(childFragment)
        }

        onView(withId(R.id.applicationIconView)).check(matches(isDisplayed()))
        onView(withId(R.id.fab)).check(matches(isDisplayed()))
        onView(withId(R.id.bottomNavigation)).check(matches(isDisplayed()))
        onView(withId(R.id.fragmentContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
        onView(withId(R.id.share)).check(matches(isDisplayed()))

        Intents.init()

        onView(allOf(withId(R.id.share))).perform(click())

        Intents.intended(allOf(hasAction(Intent.ACTION_CHOOSER)), Intents.times(1))

        Intents.release()
    }

    private fun lenientClick(): ViewAction =
        object : ViewAction {
            override fun getConstraints(): Matcher<View> = ViewMatchers.isEnabled()

            override fun getDescription(): String = "Barely clicking"

            override fun perform(
                uiController: UiController?,
                view: View,
            ) {
                view.performClick()
            }
        }
}
