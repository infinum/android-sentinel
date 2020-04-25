package com.infinum.sentinel.data.sources.raw

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(
    BasicCollectorTests::class,
    ToolsCollectorTests::class,
    DeviceCollectorTestSuite::class
)
class CollectorsTestSuite