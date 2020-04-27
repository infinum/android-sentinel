package com.infinum.sentinel

import com.infinum.sentinel.data.sources.raw.CollectorsTestSuite
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(
    SentinelTests::class,
    CollectorsTestSuite::class
)
class SentinelTestSuite