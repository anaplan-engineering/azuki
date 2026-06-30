package com.anaplan.engineering.azuki.rightofway.implementation

import org.junit.Test
import kotlin.test.assertEquals

class TestAirspaceV1 {

    val AIRSPACE0 = AirspaceV1(DELTA_O, DELTA_C, THETA_H, true, emptyMap())

    @Test
    fun emptyAirspace() {
        // empty adds an empty line
        val expected = """
            {

            }
        """.trimIndent()
        assertEquals(expected, AIRSPACE0.toJsonString())
    }

    @Test
    fun simpleAirspaceAddAircraft() {
        //VDM
//        P1: Position = mk_RVector(-3,3);
//        V1: Velocity = mk_RVector(2,1);
//        P2: Position = mk_RVector(3,4);
//        V2: Velocity = mk_RVector(1,2);
//        A1: Aircraft = mk_Aircraft(P1, V1);
//        A2: Aircraft = mk_Aircraft(P2, V2);
//        AIRSPACE1 : Airspace = mk_Airspace({A1, A2}, DELTA_C, DELTA_O, THETA_H, true);
        val airspace = AIRSPACE0
        airspace.addAircraft("a0", Position(-3.0, 3.0), Velocity(2.0, 1.0))
        airspace.addAircraft("a1", Position(3.0, 4.0), Velocity(1.0, 2.0))

//        val expected = """
//            { "a0": { "position": { "x": -3.0, "y": 3.0 }, "velocity": { "x": 2.0, "y": 1.0 } }, "a1": { "position": { "x": 3.0, "y": 4.0 }, "velocity": { "x": 1.0, "y": 2.0 } } }
//        """.trimIndent()
        val expected = """
            {
            "a0" : [[-3.0, 3.0], [2.0, 1.0]],
            "a1" : [[3.0, 4.0], [1.0, 2.0]]
            }
        """.trimIndent()
        assertEquals(expected, airspace.toJsonString())
    }
}
