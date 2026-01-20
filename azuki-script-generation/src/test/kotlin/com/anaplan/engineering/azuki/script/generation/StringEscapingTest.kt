package com.anaplan.engineering.azuki.script.generation

import kotlin.test.*

class StringEscapingTest {

    @Test
    fun literalWithoutEscapableCharacters() {
        assertEquals(
            expected = """"hello, world"""",
            actual = "hello, world".literal
        )
    }

    @Test
    fun literalWithTripleQuotes() {
        assertEquals(
            expected = """$triple${escapedTriple}hello, world${escapedTriple}$triple""",
            actual = "${triple}hello, world${triple}".literal
        )
    }

    @Test
    fun literalWithDollars() {
        assertEquals(
            expected = """${triple}give me $${"{'$'}"}500 please${triple}""",
            actual = "give me $500 please".literal
        )
    }

    @Test
    fun literalWithNewlines() {
        assertEquals(
            expected = """${triple}hello,$${"{'\\n'}"}world$${"{'\\n'}"}${triple}""",
            actual = "hello,\nworld\n".literal
        )
    }

    @Test
    fun literalWithCarriageReturns() {
        assertEquals(
            expected = """${triple}hello,$${"{'\\r'}"}world${triple}""",
            actual = "hello,\rworld".literal
        )
    }
}
