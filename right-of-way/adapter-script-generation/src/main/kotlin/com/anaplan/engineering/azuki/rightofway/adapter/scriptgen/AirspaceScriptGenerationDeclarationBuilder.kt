package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.dsl.AircraftBlock
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayGiven

class AirspaceScriptGenerationDeclarationBuilder(declaration: AirspaceDeclaration) :
    ScriptGenerationDeclarationBuilder<RightOfWayGenerationEnvironment, AirspaceDeclaration>(declaration) {

    // Reflective reference to function from interface with one parameter and Unit result
    private val newAirspace: kotlin.reflect.KFunction6<RightOfWayGiven, String, Double, Double, Double, Boolean, Unit> =
        RightOfWayGiven::thereIsANewAirspace
    private val airspaceWithData: kotlin.reflect.KFunction7<RightOfWayGiven, String, String, Double, Double, Double, Boolean, Unit> =
        RightOfWayGiven::thereIsAnAirspace
    private val thereIsAnAircraft: kotlin.reflect.KFunction3<AircraftBlock, String, com.anaplan.engineering.azuki.rightofway.adapter.api.Aircraft, Unit> =
        AircraftBlock::thereIsAnAircraft

    override fun getDeclarationScript(environment: RightOfWayGenerationEnvironment) =
        when {
            declaration.aircrafts.isEmpty() -> RightOfWayScriptingHelper.scriptifyFunction(
                newAirspace,
                declaration.name,
                declaration.delta_o,
                declaration.delta_c,
                declaration.Theta_h,
                declaration.opened,
            )
            declaration.airspaceData != null -> RightOfWayScriptingHelper.scriptifyFunction(
                airspaceWithData,
                declaration.name,
                // add a newline to avoid """ and board being on same line
                "\n" + declaration.airspaceData,
                declaration.delta_o,
                declaration.delta_c,
                declaration.Theta_h,
                declaration.opened,
            )
            else -> blockAirspaceScript()
        }

    private fun blockAirspaceScript(): String {
        val header = RightOfWayScriptingHelper.scriptifyFunction(
            newAirspace,
            declaration.name,
            declaration.delta_o,
            declaration.delta_c,
            declaration.Theta_h,
            declaration.opened,
        ).replace("thereIsANewAirspace", "thereIsAnAirspace")
        val aircraftLines = declaration.aircrafts.entries.joinToString("\n            ") { (aircraftName, aircraft) ->
            RightOfWayScriptingHelper.scriptifyFunction(thereIsAnAircraft, aircraftName, aircraft)
        }
        return "$header {\n            $aircraftLines\n        }"
    }

    class Factory : RightOfWayScriptGenerationDeclarationBuilderFactory<AirspaceDeclaration> {

        override val declarationClass = AirspaceDeclaration::class.java

        override fun create(declaration: AirspaceDeclaration): RightOfWayScriptGenerationDeclarationBuilder<AirspaceDeclaration> =
            AirspaceScriptGenerationDeclarationBuilder(declaration)
    }
}
