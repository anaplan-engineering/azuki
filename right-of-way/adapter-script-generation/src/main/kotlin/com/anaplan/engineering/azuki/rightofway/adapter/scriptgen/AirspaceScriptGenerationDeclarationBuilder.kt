package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.rightofway.adapter.api.toJsonString
import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayGiven

class AirspaceScriptGenerationDeclarationBuilder(declaration: AirspaceDeclaration) :
    ScriptGenerationDeclarationBuilder<RightOfWayGenerationEnvironment, AirspaceDeclaration>(declaration) {

    // Reflective reference to function from interface with one parameter and Unit result
    private val newAirspace: kotlin.reflect.KFunction6<RightOfWayGiven, String, Double, Double, Double, Boolean, Unit> =
        RightOfWayGiven::thereIsANewAirspace
    private val airspace: kotlin.reflect.KFunction3<RightOfWayGiven, String, String, Unit> =
        RightOfWayGiven::thereIsAnAirspace

    override fun getDeclarationScript(environment: RightOfWayGenerationEnvironment) =
        if (declaration.aircrafts.isEmpty()) {
            RightOfWayScriptingHelper.scriptifyFunction(
                newAirspace,
                declaration.name
            )
        } else {
            RightOfWayScriptingHelper.scriptifyFunction(
                airspace,
                declaration.name,
                // add a newline to avoid """ and board being on same line
                "\n" + declaration.aircrafts.toJsonString(),
            )
        }

    class Factory : RightOfWayScriptGenerationDeclarationBuilderFactory<AirspaceDeclaration> {

        override val declarationClass = AirspaceDeclaration::class.java

        override fun create(declaration: AirspaceDeclaration): RightOfWayScriptGenerationDeclarationBuilder<AirspaceDeclaration> =
            AirspaceScriptGenerationDeclarationBuilder(declaration)
    }
}
