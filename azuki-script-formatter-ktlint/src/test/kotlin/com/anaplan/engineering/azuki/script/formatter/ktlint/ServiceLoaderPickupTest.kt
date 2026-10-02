package com.anaplan.engineering.azuki.script.formatter.ktlint

import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Quick test to make sure KtLint is picked up by the service loader.
 */
class ServiceLoaderPickupTest {

    @Test
    fun picksUpKtLint() {
        val script = """
            object Foo {
            fun bar() {
            println("baz")
            }
            }
        """.trimIndent()

        // there are no other services on the classpath in this module
        val service = assertIs<KtLintScenarioFormatterService>(ScenarioFormatter.service)
        assertTrue(service.canFormat)

        val formattedFromService = service.formatScenario(script)
        assertEquals(ScenarioFormatter.formatScenario(script), formattedFromService)
        assertNotEquals(script, formattedFromService)
    }
}
