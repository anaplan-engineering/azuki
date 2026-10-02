package com.anaplan.engineering.azuki.script.formatter

import java.util.ServiceLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

/**
 * Quick test to make sure the scenario formatter falls back appropriately.
 */
class ScenarioFormatterFallbackTest {

    @Test
    fun fallsBackToNoScenarioFormatterService() {
        val script = """
            object Foo {
            fun bar() {
            println("baz")
            }
            }
        """.trimIndent()

        // check to see if the loader can see MockScenarioFormatterService
        assertIs<MockScenarioFormatterService>(ServiceLoader.load(ScenarioFormatterService::class.java).firstOrNull())

        // the only valid loader on the classpath in this module is MockScenarioFormatterService
        // and that returns itself as `false` for `canFormat`
        val service = assertIs<NoScenarioFormatterService>(ScenarioFormatter.service)
        assertFalse(service.canFormat)

        assertEquals(script, service.formatScenario(script))
        assertEquals(script, ScenarioFormatter.formatScenario(script))
    }
}
