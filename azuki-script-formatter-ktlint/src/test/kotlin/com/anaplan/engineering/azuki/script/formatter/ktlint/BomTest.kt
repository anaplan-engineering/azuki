package com.anaplan.engineering.azuki.script.formatter.ktlint

import kotlin.test.*

/**
 * Tests some BOM corner cases in script generation and formatting.
 */
class ScriptGenerationServiceBomTest {

    @Test
    fun bomPrefix() {
        assertEquals(BOM + expected, KtLintScenarioFormatterService().formatScenario(BOM + expected))
    }

    @Test
    fun noBomPrefix() {
        assertEquals(expected, KtLintScenarioFormatterService().formatScenario(expected))
    }

    companion object {

        const val BOM = "\uFEFF"
        val expected =
            """
            verifiableScenario {
                given {
                    thisHasABOM("${BOM}atTheStart")
                    thisHasABOM("in${BOM}TheMiddle")
                    thisHasABOM("atTheEnd${BOM}")
                    thisHasABOM("色は匂えど${BOM}散りぬるを")
                }
                then {
                    everythingIsOkay()
                }
            }
            """.trimIndent() + "\n"
    }
}
