package com.anaplan.engineering.azuki.rightofway.adapter.vdm

import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircrafts
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.vdm.toVdmSet

// TODO LF: if data classes could be extended, this would be directly on RVector in adapter-api?
fun toVdmPosition(position: Position) = "mk_${RightOfWayRulesModule.RVector}(${position.x}, ${position.y})"
fun toVdmVelocity(velocity: Velocity) = "mk_${RightOfWayRulesModule.RVector}(${velocity.x}, ${velocity.y})"
fun toVdmAircraft(aircraft: Aircraft) = "mk_${RightOfWayRulesModule.Aircraft}(${toVdmPosition(aircraft.position)}, ${toVdmVelocity(aircraft.velocity)})"
fun toVdmAircrafts(aircrafts: Aircrafts) =
    toVdmSet(aircrafts.values.map { a -> toVdmAircraft(a) } )
