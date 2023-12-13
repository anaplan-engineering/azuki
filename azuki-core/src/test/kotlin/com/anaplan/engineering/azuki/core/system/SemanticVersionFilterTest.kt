package com.anaplan.engineering.azuki.core.system

import kotlin.test.Test
import kotlin.test.assertEquals

class SemanticVersionFilterTest {

    @Test
    fun noScenarioVersion() {
        val semanticVersionFilter = SemanticVersionFilter("1.0.0")
        assertEquals(true, semanticVersionFilter.canVerify(null))
    }

    @Test
    fun withScenarioVersion() {
        val semanticVersionFilter = SemanticVersionFilter("1.0.0")
        assertEquals(true, semanticVersionFilter.canVerify("0.9.5"))
        assertEquals(true, semanticVersionFilter.canVerify("1.0.0-alpha"))
        assertEquals(true, semanticVersionFilter.canVerify("1.0.0"))
        assertEquals(false, semanticVersionFilter.canVerify("1.0.1"))
        assertEquals(false, semanticVersionFilter.canVerify("1.0.1-alpha"))
        assertEquals(false, semanticVersionFilter.canVerify("2.1.0"))
    }

}
