package com.infinum.sentinel.ui.deeplinks.launch

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.data.sources.raw.deeplinks.fixtures.FixtureLinksActivity
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Launches links from the fixture activity. Launch tests stub VIEW intents so nothing really opens; the
 * no-handler tests don't stub, so the system reports a missing activity.
 */
@SmallTest
@RunWith(AndroidJUnit4::class)
internal class DeepLinkLauncherTests {
    private companion object {
        const val HOME_LINK = "sentinelsample://home"
        const val UNHANDLED_LINK = "sentinelnohandler://x"
        const val PACKAGE_NAME = "com.infinum.sentinel.tool.deeplinks.test"
    }

    private var isIntentsInitialized = false

    @After
    fun tearDown() {
        if (isIntentsInitialized) Intents.release()
    }

    @Test
    fun realTap_buildsBrowsableViewIntentWithoutPackage() {
        val result = launchStubbed(HOME_LINK, LaunchMode.REAL_TAP)

        assertEquals(LaunchResult.Launched, result)
        val intent = recordedViewIntent()
        assertEquals(HOME_LINK, intent.dataString)
        assertEquals(setOf(Intent.CATEGORY_BROWSABLE), intent.categories)
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertNull(intent.`package`)
        assertNull(intent.component)
    }

    @Test
    fun openInApp_buildsPackageScopedViewIntentWithoutCategory() {
        val result = launchStubbed(HOME_LINK, LaunchMode.OPEN_IN_APP)

        assertEquals(LaunchResult.Launched, result)
        val intent = recordedViewIntent()
        assertEquals(HOME_LINK, intent.dataString)
        assertTrue(intent.categories.isNullOrEmpty())
        assertTrue(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0)
        assertEquals(PACKAGE_NAME, intent.`package`)
    }

    @Test
    fun realTap_unknownScheme_returnsNoHandler() {
        assertEquals(LaunchResult.NoHandler, launch(UNHANDLED_LINK, LaunchMode.REAL_TAP))
    }

    @Test
    fun openInApp_unhandledLink_returnsNotHandledByApp() {
        assertEquals(LaunchResult.NotHandledByApp, launch(UNHANDLED_LINK, LaunchMode.OPEN_IN_APP))
    }

    private fun launchStubbed(
        link: String,
        mode: LaunchMode,
    ): LaunchResult {
        Intents.init()
        isIntentsInitialized = true
        Intents
            .intending(hasAction(Intent.ACTION_VIEW))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
        return launch(link, mode)
    }

    private fun launch(
        link: String,
        mode: LaunchMode,
    ): LaunchResult {
        var result: LaunchResult? = null
        ActivityScenario.launch(FixtureLinksActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> result = DeepLinkLauncher(activity).launch(link, mode) }
        }
        return requireNotNull(result)
    }

    private fun recordedViewIntent(): Intent =
        Intents
            .getIntents()
            .filter { it.action == Intent.ACTION_VIEW }
            .single()
}
