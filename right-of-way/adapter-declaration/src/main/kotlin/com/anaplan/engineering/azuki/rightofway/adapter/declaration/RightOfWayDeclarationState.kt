package com.anaplan.engineering.azuki.rightofway.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.THETA_H
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_C
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import kotlin.collections.plus

class RightOfWayDeclarationState : DeclarationState() {
    fun declareAirspace(airspaceName: String, deltaO: Double = DELTA_C, deltaC: Double = DELTA_O, thetaH: Double = THETA_H) {
        checkForDuplicate(airspaceName)
        declarations[airspaceName] = AirspaceDeclaration(airspaceName, emptyMap(), deltaO, deltaC, thetaH)
    }

    fun declareAircraft(airspaceName: String, aircraftName: String, aircraft: Aircraft) {
        checkForDuplicateAircraft(airspaceName, aircraftName)
        val airspace = getDeclaration<AirspaceDeclaration>(airspaceName)
        declarations[airspaceName] = airspace.copy(
            aircrafts = airspace.aircrafts.plus(aircraftName to aircraft))
    }

    private fun checkForDuplicateAircraft(airspaceName: String, aircraftName: String) {
        checkExists(airspaceName)
        val airspace = getDeclaration<AirspaceDeclaration>(airspaceName)
        if (airspace.aircrafts.containsKey(aircraftName)) throw DuplicateDeclarationException(aircraftName)
    }
}
