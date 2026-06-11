package com.anaplan.engineering.azuki.rightofway.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.api.Theta_h
import com.anaplan.engineering.azuki.rightofway.adapter.api.Velocity
import com.anaplan.engineering.azuki.rightofway.adapter.api.delta_c
import com.anaplan.engineering.azuki.rightofway.adapter.api.delta_o
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import kotlin.collections.plus

class RightOfWayDeclarationState : DeclarationState() {
    fun declareAirspace(airspaceName: String, deltaO: Double = delta_c, deltaC: Double = delta_o, thetaH: Double = Theta_h) {
        checkForDuplicate(airspaceName)
        declarations[airspaceName] = AirspaceDeclaration(airspaceName, emptyMap(), deltaO, deltaC, thetaH)
    }

    fun declareAircraft(airspaceName: String, aircraftName: String, position: Position, velocity: Velocity) {
        checkForDuplicateAircraft(airspaceName, aircraftName)
        val airspace = getDeclaration<AirspaceDeclaration>(airspaceName)
        declarations[airspaceName] = airspace.copy(
            aircrafts = airspace.aircrafts.plus(
                aircraftName to Aircraft(position to velocity)))
    }

    private fun checkForDuplicateAircraft(airspaceName: String, aircraftName: String) {
        checkExists(airspaceName)
        val airspace = getDeclaration<AirspaceDeclaration>(airspaceName)
        if (airspace.aircrafts.containsKey(aircraftName)) throw DuplicateDeclarationException(aircraftName)
    }
}
