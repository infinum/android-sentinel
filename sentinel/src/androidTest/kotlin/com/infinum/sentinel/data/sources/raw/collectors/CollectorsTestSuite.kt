package com.infinum.sentinel.data.sources.raw.collectors

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(
    ToolsCollectorTests::class,
    DeviceCollectorTestSuite::class,
    ApplicationCollectorTests::class,
    PermissionsCollectorTests::class,
    PreferencesCollectorTests::class
)
class CollectorsTestSuite