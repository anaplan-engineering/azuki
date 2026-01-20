package com.anaplan.engineering.azuki.script.generation.runnable

import com.anaplan.engineering.azuki.core.runner.AdapterTest
import com.anaplan.engineering.azuki.core.runner.AnalysisScenario
import com.anaplan.engineering.azuki.core.runner.Eac
import com.anaplan.engineering.azuki.core.runner.GeneratedScenario
import com.anaplan.engineering.azuki.core.runner.Issue
import com.anaplan.engineering.azuki.core.runner.KnownBug
import com.anaplan.engineering.azuki.core.scenario.Since
import com.anaplan.engineering.azuki.core.system.BEH
import com.anaplan.engineering.azuki.core.system.Behavior
import com.anaplan.engineering.azuki.core.system.FunctionalElement
import com.anaplan.engineering.azuki.core.system.ImplementationVersion
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationService
import com.anaplan.engineering.azuki.script.generation.VerifiableScenarioScript
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

                @KnownBug(Issue("BarImpl", "1.4"))
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

                @KnownBug(Issue(Bar, "1.4"))
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
            exampleScript.render {
                identifierMapper = ExampleMapper
            }
        }
    }

    @Test
    fun renderAllScenarioTypes() {
        val scenarioTypes = mapOf(
            "normalAdapter" to AdapterTest(false).toMethodType(),
            "skippedAdapter" to AdapterTest(true).toMethodType(),
            "analysis" to AnalysisScenario().toMethodType(),
            "eac" to Eac("foo", "bar", "baz").toMethodType(),
            "generated" to GeneratedScenario().toMethodType(),
            "custom" to object : RunnableScenarioMethodType {
                override val annotation = AnalysisScenario()
                override val kotlinName = QualifiedIdentifier.create("com.example", "CustomScenario")
            }
        )

        val expected = """
            package com.example

            import com.anaplan.engineering.azuki.core.runner.*
            import com.anaplan.engineering.azuki.core.system.*
            import com.example.AcmeRunnableScenario
            import com.example.AllTheAnnotations
            import com.example.CustomScenario

            class AllTheAnnotations : AcmeRunnableScenario() {

                @AdapterTest
                fun normalAdapter() {
                }

                @AdapterTest(expectSkip = true)
                fun skippedAdapter() {
                }

                @AnalysisScenario
                fun analysis() {
                }

                @Eac("foo", "bar", "baz")
                fun eac() {
                }

                @GeneratedScenario
                fun generated() {
                }

                @CustomScenario
                fun custom() {
                }
            }
        """.trimIndent() + "\n"

        expect(expected) {
            val methods = scenarioTypes.map { (name, type) ->
                RunnableScenarioClassScript.MethodScript(
                    name = name,
                    type = type,
                    annotations = emptySet(),
                    body = emptyScenario,
                )
            }

            val script = RunnableScenarioClassScript(
                testName = QualifiedIdentifier.create("com.example", "AllTheAnnotations"),
                baseName = QualifiedIdentifier.create("com.example", "AcmeRunnableScenario"),
                methods = methods,
                beh = null
            )

            script.render()
        }
    }

    companion object {

        private val emptyScenario: VerifiableScenarioScript by lazy {
            ScriptGenerationService.standalone.given {
            }.whenever {
            }.then {
            }.verifiableScenario
        }

        private val exampleScenario: VerifiableScenarioScript by lazy {
            ScriptGenerationService.standalone.given {
                +"thereIsAFoo()"
            }.whenever {
                +"aThingHappens()"
            }.then {
                +"thereIsNoFoo()"
            }.verifiableScenario
        }

        private val exampleScript: RunnableScenarioClassScript by lazy {

            val method = RunnableScenarioClassScript.MethodScript(
                name = "test",
                type = Eac("Tests a thing", "A thing should happen").toMethodType(),
                annotations = setOf(
                    Since(ImplementationVersion("BarImpl", "1.0")),
                    KnownBug(Issue("BarImpl", "1.4"))
                ),
                body = exampleScenario,
            )

            RunnableScenarioClassScript(
                testName = QualifiedIdentifier.create("com.example", "Foo"),
                baseName = QualifiedIdentifier.create("com.example", "AcmeRunnableScenario"),
                methods = listOf(method),
                beh = BEH(behavior = 12, functionalElement = 345, summary = "Does things"),
            )
        }

        /**
         * Set up reverse maps to convert various parts of the example script into their Kotlin identifiers.
         */
        private object ExampleMapper: IdentifierMapper {
            // mapping backwards from behavior 12 to com.example.AcmeBehaviors.Foo
            override fun getBehaviorIdentifier(beh: Behavior) =
                (QualifiedIdentifier.create("com.example", "AcmeBehaviors") dot "Foo").takeIf { beh == 12 }
            // mapping backwards from FE 345 to com.example.ThingFe
            override fun getFunctionalElementIdentifier(fe: FunctionalElement) =
                (QualifiedIdentifier.create("com.example", "AcmeFunctionalElements") dot "Thing").takeIf { fe == 345 }
            // mapping backwards from implementation BarImpl to com.example.Bar
            override fun getImplementationIdentifier(impl: String) =
                QualifiedIdentifier.create("com.example", "Bar").takeIf { impl == "BarImpl" }
        }
    }
}
