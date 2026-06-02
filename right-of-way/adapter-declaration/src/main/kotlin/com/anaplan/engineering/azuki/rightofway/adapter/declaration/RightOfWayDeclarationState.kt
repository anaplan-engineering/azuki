package com.anaplan.engineering.azuki.rightofway.adapter.declaration

import com.anaplan.engineering.azuki.declaration.DeclarationState
import com.anaplan.engineering.azuki.rightofway.adapter.api.Position
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AircraftDeclaration

class RightOfWayDeclarationState : DeclarationState() {
    fun declareAirspace(airspaceName: String) {
        checkForDuplicate(airspaceName)
        declarations[airspaceName] = AirspaceDeclaration(airspaceName)
    }

    fun declareAircraft(airspaceName: String, aircraftName: String) {
        checkExists(airspaceName)
        checkForDuplicate(aircraftName)
        declarations[airspaceName] = AircraftDeclaration(aircraftName)
    }

    fun aircraftMove(airspaceName: String, aircraftName: String, position: Position) {
        val game = getDeclaration<AirspaceDeclaration>(airspaceName)
        //declarations[gameName] = game.copy(moves = game.moves.plus(position to playerName))
    }
}
