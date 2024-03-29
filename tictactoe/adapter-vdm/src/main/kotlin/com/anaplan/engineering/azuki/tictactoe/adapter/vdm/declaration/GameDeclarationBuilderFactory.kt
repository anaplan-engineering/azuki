package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.GameDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.VdmGenerationException
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmMoves
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.VdmDeclaration

class GameDeclarationBuilderFactory : VdmDeclarationBuilderFactory<GameDeclaration> {

    override val declarationClass = GameDeclaration::class.java
    override fun create(declaration: GameDeclaration): VdmDeclarationBuilder<GameDeclaration> =
        GameDeclarationBuilder(declaration)

    private class GameDeclarationBuilder(declaration: GameDeclaration) :
        VdmDeclarationBuilder<GameDeclaration>(declaration) {

        override fun imports(builder: DefaultModuleBuilder) =
            setOf(
                XOModule.Game.import,
                XOModule.Pos.import,
            )

        override fun declarations(builder: DefaultModuleBuilder, container: Declaration?): List<VdmDeclaration> {
            val playOrderGetter = builder.getters[declaration.orderName]
                ?: throw VdmGenerationException("Missing getter for model ${declaration.orderName}")
            return listOf(VdmDeclaration(declaration.vdmName(),
                XOModule.Game,
                "mk_${XOModule.Game}(${toVdmMoves(declaration.moves)}, $playOrderGetter)"))
        }
    }
}
