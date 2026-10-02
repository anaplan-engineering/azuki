package com.anaplan.engineering.azuki.script.generation

import com.anaplan.engineering.azuki.script.formatter.NoScenarioFormatterService
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatter
import com.anaplan.engineering.azuki.script.formatter.ScenarioFormatterService
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import kotlin.test.*

/**
 * Tests some BOM corner cases in script generation and formatting.
 */
@RunWith(Parameterized::class)
class ScriptGenerationServiceBomTest(
    private var name: String, private var formatter: ScenarioFormatterService, private var prefix: String
) {

    @Test
    fun test() {
        expect(prefix + expected, "problem with BOMs in case $name") {
            ScriptGenerationService.standalone.bomScenario().render {
                formatter = if (prefix.isNotEmpty()) {
                    // slipstream in the prefix
                    ScenarioFormatterService { this@ScriptGenerationServiceBomTest.formatter.formatScenario(prefix + it) }
                } else {
                    this@ScriptGenerationServiceBomTest.formatter
                }
            }.trim()
        }
    }

    companion object {

        @Parameterized.Parameters(name = "{0}")
        @JvmStatic
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf("unformatted", NoScenarioFormatterService, ""),
            arrayOf("formatted", ScenarioFormatter, ""),
            arrayOf("unformatted with BOM", NoScenarioFormatterService, BOM),
            arrayOf("formatted with BOM", ScenarioFormatter, BOM),
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
