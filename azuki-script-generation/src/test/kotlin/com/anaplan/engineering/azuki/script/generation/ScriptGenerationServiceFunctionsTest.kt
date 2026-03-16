package com.anaplan.engineering.azuki.script.generation

import kotlin.test.*

class ScriptGenerationServiceFunctionsTest {
    @Test
    fun renderScriptFunctionsExampleScenario() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(a)
                    thereIsABar(a, b)
                    thereIsABaz(
                        a, a,
            ${"\"\"\""}
            a b c
            d e f
            g h i
            ${"\"\"\""},
                        b,
            ${"\"\"\""}
            a b c
            d e f
            g h i
            ${"\"\"\""},
                    )
                }
                then {
                    everythingIsOkay()
                }
            }
        """.trimIndent()

        assertEquals(expected, generateAndRender {
            given {
                +scriptFunction("thereIsAFoo", argA)
                +scriptFunction("thereIsABar", argA, argB)
                +scriptFunction("thereIsABaz", argA, argA, longArg, argB, longArg)
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    @Test
    fun renderScriptFunctionsWithHorizontalVerticalHorizontalArguments() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(
                        a, b,
            ${"\"\"\""}
            a b c
            d e f
            g h i
            ${"\"\"\""},
                        b, a,
                    )
                }
                then {
                    everythingIsOkay()
                }
            }
        """.trimIndent()

        assertEquals(expected, generateAndRender {
            given {
                +scriptFunction("thereIsAFoo", argA, argB, longArg, argB, argA)
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    @Test
    fun renderScriptFunctionsWithVerticalThenHorizontalArguments() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(
            $TRIPLE
            a b c
            d e f
            g h i
            $TRIPLE,
                        a, b,
                    )
                }
                then {
                    everythingIsOkay()
                }
            }
        """.trimIndent()

        assertEquals(expected, generateAndRender {
            given {
                +scriptFunction("thereIsAFoo", longArg, argA, argB)
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    @Test
    fun renderScriptFunctionsWithHorizontalThenVerticalArguments() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(
                        a, b,
            $TRIPLE
            a b c
            d e f
            g h i
            $TRIPLE,
                    )
                }
                then {
                    everythingIsOkay()
                }
            }
        """.trimIndent()

        assertEquals(expected, generateAndRender {
            given {
                +scriptFunction("thereIsAFoo", argA, argB, longArg)
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    @Test
    fun renderScriptFunctionsWithAllHorizontalArguments() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(a, b, b, a)
                }
                then {
                    everythingIsOkay()
                }
            }
        """.trimIndent()

        assertEquals(expected, generateAndRender {
            given {
                +scriptFunction("thereIsAFoo", argA, argB, argB, argA)
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    @Test
    fun renderScriptFunctionsWithAllVerticalArguments() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(
            $TRIPLE
            a b c
            d e f
            g h i
            $TRIPLE,
            $TRIPLE
            a b c
            d e f
            g h i
            $TRIPLE,
                    )
                }
                then {
                    everythingIsOkay()
                }
            }
        """.trimIndent()

        assertEquals(expected, generateAndRender {
            given {
                +scriptFunction("thereIsAFoo", longArg, longArg)
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    companion object {

        val argA = ScriptStringFragment("a")
        val argB = ScriptStringFragment("b")
        val longArg = ScriptStringFragment("${TRIPLE}\na b c\nd e f\ng h i\n${TRIPLE}")
        const val TRIPLE = "\"\"\""

        val everythingIsOkay = scriptFunction("everythingIsOkay")

        fun scriptFunction(name: String, vararg args: ScriptStringFragment) = ScriptFunction(name, listOf(*args))

        fun generateAndRender(
            body: ScriptGenerationService<*, *, *, *, *, *, *>.() -> ScenarioScript
        ) = ScriptGenerationService.standalone.body().render {
            // for these tests we want to see if the standard formatting is good enough
            formatter = Formatter.None
        }.trim()
    }
}
