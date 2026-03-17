package com.anaplan.engineering.azuki.script.generation

import kotlin.test.*

class ScriptGenerationServiceFunctionsTest {

    @Test
    fun renderScriptFunctionsAllHorizontal() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(a)
                    thereIsABar(a, b)
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
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    @Test
    fun renderScriptFunctionsHorizontalVerticalHorizontal() {
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
    fun renderScriptFunctionsVerticalHorizontalVertical() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(
            $TRIPLE
            a b c
            d e f
            g h i
            $TRIPLE,
                        a, b, a,
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
                +scriptFunction("thereIsAFoo", longArg, argA, argB, argA, longArg)
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    @Test
    fun renderScriptFunctionsAllVertical() {
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

    @Test
    fun renderScriptFunctionsNestedHorizontal() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(bar(a, b))
                }
                then {
                    everythingIsOkay()
                }
            }
        """.trimIndent()

        assertEquals(expected, generateAndRender {
            given {
                +scriptFunction("thereIsAFoo", scriptFunction("bar", argA, argB))
            }.whenever {
            }.then {
                +everythingIsOkay
            }.verifiableScenario
        })
    }

    @Test
    fun renderScriptFunctionsNestedVertical() {
        val expected = """
            verifiableScenario {
                given {
                    thereIsAFoo(
                        bar(
            $TRIPLE
            a b c
            d e f
            g h i
            $TRIPLE,
                        ),
                    )
                }
                then {
                    everythingIsOkay()
                }
            }
        """.trimIndent()

        assertEquals(expected, generateAndRender {
            given {
                +scriptFunction("thereIsAFoo", scriptFunction("bar", longArg))
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

        fun scriptFunction(name: String, vararg args: ScriptElement) = ScriptFunction(name, listOf(*args))

        fun generateAndRender(
            body: ScriptGenerationService<*, *, *, *, *, *, *>.() -> ScenarioScript
        ) = ScriptGenerationService.standalone.body().render {
            // for these tests we want to see if the standard formatting is good enough
            formatter = Formatter.None
        }.trim()
    }
}
