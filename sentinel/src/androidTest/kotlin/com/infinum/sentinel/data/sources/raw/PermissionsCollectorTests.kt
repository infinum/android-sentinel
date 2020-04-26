package com.infinum.sentinel.data.sources.raw

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
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
internal class PermissionsCollectorTests {

    companion object {

        private val APPENDED_PERMISSIONS = mapOf(Manifest.permission.REORDER_TASKS to true)

        lateinit var actualPermissions: Map<String, Boolean>

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            val context =
                ApplicationProvider.getApplicationContext<SentinelTestApplication>().applicationContext

            val collector = PermissionsCollector(context)
            collector.collect()

            actualPermissions = collector.present()
        }
    }

    @Test
    @SmallTest
    fun permissions_notDeclared() {
        assertTrue(actualPermissions.isNotEmpty())
        assertEquals(APPENDED_PERMISSIONS.size, actualPermissions.size)
        assertEquals(
            APPENDED_PERMISSIONS.containsKey(Manifest.permission.REORDER_TASKS),
            actualPermissions.containsKey(Manifest.permission.REORDER_TASKS)
        )
        assertEquals(
            APPENDED_PERMISSIONS[Manifest.permission.REORDER_TASKS],
            actualPermissions[Manifest.permission.REORDER_TASKS]
        )
    }
}
