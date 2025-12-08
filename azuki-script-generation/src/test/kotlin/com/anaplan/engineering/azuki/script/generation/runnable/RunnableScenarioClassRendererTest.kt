package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.core.system.ImplementationVersion
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationService
import com.anaplan.engineering.azuki.script.generation.runnable.RunnableScenarioClassRenderer.Companion.render
import kotlin.test.*

class RunnableScenarioClassRendererTest {

    @Test
    fun renderWithoutKotlinNames() {
        // note that we expect the final class to end with a blank newline,
        // and the lack of indentation in the given/whenever/then blocks is intentional.
        val expected = """
            package com.example

            import com.anaplan.engineering.azuki.core.runner.*
            import com.anaplan.engineering.azuki.core.scenario.Since
            import com.anaplan.engineering.azuki.core.system.*
            import com.example.AcmeRunnableScenario
            import com.example.Foo

            @BEH(12, 345, "Does things")
            class Foo : AcmeRunnableScenario() {

                @Since(ImplementationVersion("BarImpl", "1.0"))
                @Eac("Tests a thing", "A thing should happen")
                fun test() {
                    given {
            thereIsAFoo()
                    }
                    whenever {
            aThingHappens()
                    }
                    then {
            thereIsNoFoo()
                    }
                }
            }
        """.trimIndent() + "\n"

        expect(expected) {
            exampleScript.render()
        }
    }

    @Test
    fun renderWithKotlinNames() {
        // note that we expect the final class to end with a blank newline,
        // and the lack of indentation in the given/whenever/then blocks is intentional.
        val expected = """
            package com.example

            import com.anaplan.engineering.azuki.core.runner.*
            import com.anaplan.engineering.azuki.core.scenario.Since
            import com.anaplan.engineering.azuki.core.system.*
            import com.example.AcmeBehaviors
            import com.example.AcmeFunctionalElements
            import com.example.AcmeRunnableScenario
            import com.example.Bar
            import com.example.Foo

            @BEH(AcmeBehaviors.Foo, AcmeFunctionalElements.Thing, "Does things")
            class Foo : AcmeRunnableScenario() {

                @Since(ImplementationVersion(Bar, "1.0"))
                @Eac("Tests a thing", "A thing should happen")
                fun test() {
                    given {
            thereIsAFoo()
                    }
                    whenever {
            aThingHappens()
                    }
                    then {
            thereIsNoFoo()
                    }
                }
            }
        """.trimIndent() + "\n"

        expect(expected) {
            exampleScript.render { setupExampleRenderer() }
        }
    }

    companion object {

        private val exampleScript: RunnableScenarioClassScript by lazy {
            val scenario = ScriptGenerationService.standalone.given {
                +"thereIsAFoo()"
            }.whenever {
                +"aThingHappens()"
            }.then {
                +"thereIsNoFoo()"
            }.verifiableScenario

            val method = RunnableScenarioClassScript.MethodScript(
                name = "test",
                type = EacMethodType(Eac("Tests a thing", "A thing should happen")),
                annotations = RunnableScenarioAnnotations(since = Since(ImplementationVersion("BarImpl", "1.0"))),
                body = scenario,
            )

            RunnableScenarioClassScript(
                testName = KotlinName.create("com.example", "Foo"),
                baseName = KotlinName.create("com.example", "AcmeRunnableScenario"),
                methods = listOf(method),
                beh = BEH(behavior = 12, functionalElement = 345, summary = "Does things"),
            )
        }

        /**
         * Set up reverse maps to convert various parts of the example script into their Kotlin identifiers.
         */
        private fun RunnableScenarioClassRenderer.setupExampleRenderer() {
            getBehaviourKotlinName = { b ->
                // mapping backwards from behavior 12 to com.example.AcmeBehaviors.Foo
                (KotlinName.create("com.example", "AcmeBehaviors") dot "Foo").takeIf { b == 12 }
            }
            getFunctionalElementKotlinName = { fe ->
                // mapping backwards from FE 345 to com.example.ThingFe
                (KotlinName.create("com.example", "AcmeFunctionalElements") dot "Thing").takeIf { fe == 345 }
            }
            getImplementationKotlinName = { impl ->
                // mapping backwards from implementation BarImpl to com.example.Bar
                KotlinName.create("com.example", "Bar").takeIf { impl == "BarImpl" }
            }
        }
    }
}
