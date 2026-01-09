package com.anaplan.engineering.azuki.script.generation

import kotlin.test.*

/**
 * Tests using a script generation service with string fragments (therefore not needing to have an adapter).
 *
 * Tests that need an adapter should go in the test buckets for the corresponding examples.
 */
class ScriptGenerationServiceStringFragmentsTest {

    @Test
    fun incompleteScenario() {
        expect("""
            verifiableScenario {
                given {
                    thereIsAFoo(fooA)
                    thereIsAFoo(fooB)
                }
                whenever {
                    deleteFoo(fooA)
                }
            }
        """.trimIndent()) {
            generateAndRender {
                given {
                    +"thereIsAFoo(fooA)"
                    +"thereIsAFoo(fooB)"
                }.whenever {
                    +"deleteFoo(fooA)"
                }.then {}.verifiableScenario
            }
        }
    }

    @Test
    fun verifiableScenario() {
        expect("""
            verifiableScenario {
                given {
                    thereIsAFoo(fooA)
                    thereIsAFoo(fooB)
                }
                whenever {
                    deleteFoo(fooA)
                }
                then {
                    fooExists(fooA)
                    fooDoesNotExist(fooB)
                }
            }
        """.trimIndent()) {
            generateAndRender {
                given {
                    +"thereIsAFoo(fooA)"
                    +"thereIsAFoo(fooB)"
                }.whenever {
                    +"deleteFoo(fooA)"
                }.then {
                    +"fooExists(fooA)"
                    +"fooDoesNotExist(fooB)"
                }.verifiableScenario
            }
        }
    }

    @Test
    fun queryScenario() {
        expect("""
            queryScenario {
                given {
                    thereIsAFoo(fooA)
                    thereIsAFoo(fooB)
                }
                whenever {
                    deleteFoo(fooA)
                }
                query {
                    fooExists(fooA)
                    fooExists(fooB)
                }
            }
        """.trimIndent()) {
            generateAndRender {
                given {
                    +"thereIsAFoo(fooA)"
                    +"thereIsAFoo(fooB)"
                }.whenever {
                    +"deleteFoo(fooA)"
                }.query {
                    +"fooExists(fooA)"
                    +"fooExists(fooB)"
                }.queryScenario
            }
        }
    }

    @Test
    fun oracleScenario() {
        expect("""
            oracleScenario {
                given {
                    thereIsAFoo(fooA)
                }
                generate {
                    createFoo(fooB)
                    createFoo(fooC)
                }
                generate {
                    createBar(barA)
                }
                whenever {
                    deleteFoo(fooA)
                }
                generate {
                    deleteARandomFoo()
                }
                generate {
                    deleteARandomBar()
                }
                verify {
                    fooExists(fooA)
                    fooExists(fooB)
                    fooExists(fooC)
                    barExists(barA)
                }
            }
        """.trimIndent()) {
            generateAndRender {
                given {
                    +"thereIsAFoo(fooA)"
                }.generate {
                    +"createFoo(fooB)"
                    +"createFoo(fooC)"
                }.generate {
                    +"createBar(barA)"
                }.whenever {
                    +"deleteFoo(fooA)"
                }.generate {
                    +"deleteARandomFoo()"
                }.generate {
                    +"deleteARandomBar()"
                }.verify {
                    +"fooExists(fooA)"
                    +"fooExists(fooB)"
                    +"fooExists(fooC)"
                    +"barExists(barA)"
                }.oracleScenario
            }
        }
    }

    @Test
    fun oracleScenarioWithoutGenerate() {
        expect("""
            oracleScenario {
                given {
                    thereIsAFoo(fooA)
                }
                whenever {
                    deleteFoo(fooA)
                }
                verify {
                    fooExists(fooA)
                }
            }
        """.trimIndent()) {
            generateAndRender {
                given {
                    +"thereIsAFoo(fooA)"
                }.generate {
                    // this can be left empty
                }.whenever {
                    +"deleteFoo(fooA)"
                }.generate {
                    // this can be left empty
                }.verify {
                    +"fooExists(fooA)"
                }.oracleScenario
            }
        }
    }

    @Test
    fun oracleScenarioPreserveEmptyWheneverIfWhenGenerate() {
        expect("""
            oracleScenario {
                given {
                    thereIsAFoo(fooA)
                }
                whenever {
                }
                generate {
                    deleteARandomFoo()
                }
                verify {
                    fooExists(fooA)
                }
            }
        """.trimIndent()) {
            generateAndRender {
                given {
                    +"thereIsAFoo(fooA)"
                }.generateBlocks { /* this can be left empty */ }.whenever {
                    /* leaving this empty shouldn't cause whenever to disappear!
                     * it's needed to separate the given-generate and when-generate phases
                     */
                }.generate {
                    +"deleteARandomFoo()"
                }.verify {
                    +"fooExists(fooA)"
                }.oracleScenario
            }
        }
    }

    @Test
    fun customIndent() {
        expect("""
            verifiableScenario {
            ..given {
            thereIsAFoo()
            ..}
            ..then {
            ....everythingIsOkay()
            ..}
            }
        """.trimIndent()) {
            ScriptGenerationService.standalone.given {
                // this won't indent automatically when formatting is turned off
                +"thereIsAFoo()"
            }.whenever {
                // deliberately left empty
            }.then {
                +ScriptElement { ctx -> "${ctx.indent}everythingIsOkay()" }
            }.verifiableScenario.render {
                indentString = ".."
                // this isn't valid Kotlin code, so we can't format it!
                formatter = Formatter.None
            }
        }
    }

    companion object {

        fun generateAndRender(
            body: ScriptGenerationService<*, *, *, *, *, *, *>.() -> ScenarioScript
        ) = ScriptGenerationService.standalone.body().render().trim()
    }
}
