package com.anaplan.engineering.azuki.script.generation

import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.*

/**
 * Tests some BOM corner cases in script generation and formatting.
 */
@RunWith(Parameterized::class)
class ScriptGenerationServiceBomTest(
    private var name: String, private var formatter: Formatter, private var prefix: String
) {

    @Test
    fun test() {
        val script = ScriptGenerationService.standalone.bomScenario()
        val actual = script.render {
            formatter = if (prefix.isNotEmpty()) {
                // slipstream in the prefix
                Formatter { this@ScriptGenerationServiceBomTest.formatter.format(prefix + it) }
            } else {
                this@ScriptGenerationServiceBomTest.formatter
            }
        }.trim()
        assertEquals(prefix + expected, actual, "problem with BOMs in case $name")
    }

    companion object {

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf("unformatted", Formatter.None, ""),
            arrayOf("formatted", Formatter.Full, ""),
            arrayOf("unformatted with BOM", Formatter.None, BOM),
            arrayOf("formatted with BOM", Formatter.Full, BOM),
        )

        const val BOM = "\uFEFF"
        val expected by lazy {
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
            """.trimIndent()
        }

        fun ScriptGenerationService<*, *, *, *, *, *, *>.bomScenario(): ScenarioScript = given {
            +"        thisHasABOM(\"${BOM}atTheStart\")"
            +"        thisHasABOM(\"in${BOM}TheMiddle\")"
            +"        thisHasABOM(\"atTheEnd${BOM}\")"
            +"        thisHasABOM(\"色は匂えど${BOM}散りぬるを\")"
        }.whenever {}.then {
            +"        everythingIsOkay()"
        }.verifiableScenario
    }
}
