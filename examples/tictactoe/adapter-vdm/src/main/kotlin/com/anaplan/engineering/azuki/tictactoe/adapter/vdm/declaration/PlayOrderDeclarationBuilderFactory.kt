package com.anaplan.engineering.azuki.tictactoe.adapter.vdm.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.tictactoe.adapter.declaration.declaration.PlayOrderDeclaration
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.XOModule
import com.anaplan.engineering.azuki.tictactoe.adapter.vdm.toVdmPlayer
import com.anaplan.engineering.azuki.vdm.DefaultModuleBuilder
import com.anaplan.engineering.azuki.vdm.VdmDeclaration
import com.anaplan.engineering.azuki.vdm.toVdmSequence

class PlayOrderDeclarationBuilderFactory : VdmDeclarationBuilderFactory<PlayOrderDeclaration> {


    private class PlayOrderDeclarationBuilder(declaration: PlayOrderDeclaration) :
        VdmDeclarationBuilder<PlayOrderDeclaration>(declaration) {

        override fun imports(builder: DefaultModuleBuilder) =
            setOf(
                XOModule.PlayOrder.import,
            )

        override fun declarations(builder: DefaultModuleBuilder, container: Declaration?) =
            listOf(VdmDeclaration(declaration.vdmName(),
                XOModule.PlayOrder,
                toVdmSequence(declaration.playOrder.map { toVdmPlayer(it) })))
    }

    override val declarationClass = PlayOrderDeclaration::class.java

    override fun create(declaration: PlayOrderDeclaration): VdmDeclarationBuilder<PlayOrderDeclaration> =
        PlayOrderDeclarationBuilder(declaration)

}
