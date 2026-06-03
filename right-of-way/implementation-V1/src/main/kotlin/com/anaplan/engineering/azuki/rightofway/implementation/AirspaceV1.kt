package com.anaplan.engineering.azuki.rightofway.implementation

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.collections.component1
import kotlin.collections.component2

const val DELTA_O = 100.0
const val DELTA_C = 900.0
const val THETA_H = 50.0

class AirspaceV1 internal constructor(
    state: AirspaceState
) : Airspace(state) {

    constructor(prepopulated: AircraftData = emptyMap()) :
        this(
            AirspaceState(DELTA_O, DELTA_C, THETA_H, prepopulated.toMutableMap()
//                prepopulated.mapValues { (_, value) ->
//                    val (p, v) = value
//                    Aircraft(Position(p), Velocity(v))
//                }.toMutableMap()
            )
        )

    override val log: Logger = LoggerFactory.getLogger(AirspaceV1::class.java)

}

class AirspaceV1Creator : AirspaceCreator {

    override fun create(state: AirspaceState) = AirspaceV1(state)

    override fun create(prepopulated: AircraftData) = AirspaceV1(prepopulated)

}
