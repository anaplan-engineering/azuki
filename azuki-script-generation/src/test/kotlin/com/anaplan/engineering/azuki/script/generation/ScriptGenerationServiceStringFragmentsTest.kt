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
                    fromFragments("thereIsAFoo(fooA)", "thereIsAFoo(fooB)")
                }.whenever {
                    fromFragments("deleteFoo(fooA)")
                }.incompleteScenario()
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
                    fromFragments("thereIsAFoo(fooA)", "thereIsAFoo(fooB)")
                }.whenever {
                    fromFragments("deleteFoo(fooA)")
                }.then {
                    fromFragments("fooExists(fooA)", "fooDoesNotExist(fooB)")
                }.verifiableScenario()
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
                    fromFragments("thereIsAFoo(fooA)", "thereIsAFoo(fooB)")
                }.whenever {
                    fromFragments("deleteFoo(fooA)")
                }.query {
                    fromFragments("fooExists(fooA)", "fooExists(fooB)")
                }.queryScenario()
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
                    fromFragments("thereIsAFoo(fooA)")
                }.generate {
                    block {
                        fromFragments("createFoo(fooB)")
                        fromFragments("createFoo(fooC)")
                    }
                    block {
                        fromFragments("createBar(barA)")
                    }
                }.whenever {
                    fromFragments("deleteFoo(fooA)")
                }.generate {
                    block {
                        fromFragments("deleteARandomFoo()")
                    }
                    block {
                        fromFragments("deleteARandomBar()")
                    }
                }.verify {
                    fromFragments("fooExists(fooA)", "fooExists(fooB)", "fooExists(fooC)", "barExists(barA)")
                }.oracleScenario()
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
                    fromFragments("thereIsAFoo(fooA)")
                }.generate { /* this can be left empty */ }.whenever {
                    fromFragments("deleteFoo(fooA)")
                }.generate { /* this can be left empty */ }.verify {
                    fromFragments("fooExists(fooA)")
                }.oracleScenario()
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
                    fromFragments("thereIsAFoo(fooA)")
                }.generate { /* this can be left empty */ }.whenever {
                    /* leaving this empty shouldn't cause whenever to disappear!
                     * it's needed to separate the given-generate and when-generate phases
                     */
                }.generate {
                    block {
                        fromFragments("deleteARandomFoo()")
                    }
                }.verify {
                    fromFragments("fooExists(fooA)")
                }.oracleScenario()
            }
        }
    }

    companion object {

        fun generateAndRender(body: ScriptGenerationService<*, *, *, *, *, *>.() -> ScenarioScript) =
            ScriptGenerationService.standalone.body().render().trim()
    }
}
