package com.anaplan.engineering.azuki.rightofway.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft
import com.anaplan.engineering.azuki.rightofway.adapter.api.THETA_H
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_C
import com.anaplan.engineering.azuki.rightofway.adapter.api.DELTA_O
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import kotlin.collections.plus

// Action by action versus single go; atomicity; akin to state def in VDM (i.e. something outside the spec); initialisation
class RightOfWayDeclarationState : DeclarationState() {
    fun declareAirspace(airspaceName: String, deltaO: Double = DELTA_O, deltaC: Double = DELTA_C, thetaH: Double = THETA_H, opened: Boolean = true) {
        checkForDuplicate(airspaceName)
        declarations[airspaceName] = AirspaceDeclaration(airspaceName, emptyMap(), deltaO, deltaC, thetaH, opened)
    }

    fun setAirspace(airspaceName: String, opened: Boolean) {
        checkExists(airspaceName)
        val airspace = getDeclaration<AirspaceDeclaration>(airspaceName)
        declarations[airspaceName] = airspace.copy(opened = opened)
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
