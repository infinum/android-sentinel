package com.infinum.sentinel.data.sources.raw.collectors

import android.Manifest
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.ui.SentinelTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class PermissionsCollectorTests {
    companion object {
        private val APPENDED_PERMISSIONS =
            mapOf(
                Manifest.permission.REORDER_TASKS to true,
                Manifest.permission.POST_NOTIFICATIONS to false,
                Manifest.permission.WAKE_LOCK to true,
                Manifest.permission.ACCESS_NETWORK_STATE to true,
                Manifest.permission.RECEIVE_BOOT_COMPLETED to true,
                Manifest.permission.FOREGROUND_SERVICE to true,
                "com.infinum.sentinel.test.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" to true,
            )

        lateinit var actualPermissions: Map<String, Boolean>

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            val context =
                ApplicationProvider
                    .getApplicationContext<SentinelTestApplication>()
                    .applicationContext

            val collector = PermissionsCollector(context)

            actualPermissions = collector()
        }
    }

    @Test
    @SmallTest
    fun permissions_notDeclared() {
        assertTrue(actualPermissions.isNotEmpty())
        assertEquals(APPENDED_PERMISSIONS.size, actualPermissions.size)
        APPENDED_PERMISSIONS.forEach { (permission, expectedStatus) ->
            assertEquals(
                APPENDED_PERMISSIONS.containsKey(permission),
                actualPermissions.containsKey(permission),
            )
            assertEquals(
                expectedStatus,
                actualPermissions[permission],
            )
        }
    }
}
