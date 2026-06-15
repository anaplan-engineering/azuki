package com.anaplan.engineering.azuki.rightofway.adapter.scriptgen

import com.anaplan.engineering.azuki.script.generation.ScriptGenerationDeclarationBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.api.pretty
import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.rightofway.com.anaplan.engineering.azuki.rightofway.dsl.RightOfWayGiven


class AirspaceScriptGenerationDeclarationBuilder(declaration: GameDeclaration) :
    ScriptGenerationDeclarationBuilder<RightOfWayGenerationEnvironment, GameDeclaration>(declaration) {
    private val newGameOrder: kotlin.reflect.KFunction3<RightOfWayGiven, String, String, Unit> =
        RightOfWayGiven::thereIsANewGame
    private val gameOrder: kotlin.reflect.KFunction4<RightOfWayGiven, String, String, String, Unit> =
        RightOfWayGiven::thereIsAGame

    override fun getDeclarationScript(environment: RightOfWayGenerationEnvironment) =
        if (declaration.moves.isEmpty()) {
            RightOfWayScriptingHelper.scriptifyFunction(
                newGameOrder,
                declaration.name,
                declaration.orderName,
            )
        } else {
            RightOfWayScriptingHelper.scriptifyFunction(
                gameOrder,
                declaration.name,
                declaration.orderName,
                // add a newline to avoid """ and board being on same line
                "\n" + declaration.moves.pretty(3, 3),
            )
        }

    class Factory : RightOfWayScriptGenerationDeclarationBuilderFactory<GameDeclaration> {

        override val declarationClass = GameDeclaration::class.java

        override fun create(declaration: GameDeclaration): RightOfWayScriptGenerationDeclarationBuilder<GameDeclaration> =
            AirspaceScriptGenerationDeclarationBuilder(declaration)
    }
}
