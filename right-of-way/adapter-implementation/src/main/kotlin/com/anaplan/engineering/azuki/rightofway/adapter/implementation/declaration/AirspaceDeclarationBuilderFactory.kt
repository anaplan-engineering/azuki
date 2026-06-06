package com.anaplan.engineering.azuki.rightofway.adapter.implementation.declaration

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.adapter.implementation.ExecutionEnvironment


class AirspaceDeclarationBuilderFactory : SampleDeclarationBuilderFactory<AirspaceDeclaration> {

    override val declarationClass = AirspaceDeclaration::class.java
    override fun create(declaration: AirspaceDeclaration): SampleDeclarationBuilder<AirspaceDeclaration> =
        AirspaceDeclarationBuilder(declaration)

    private class AirspaceDeclarationBuilder(declaration: AirspaceDeclaration) :
        SampleDeclarationBuilder<AirspaceDeclaration>(declaration) {

        override fun build(env: ExecutionEnvironment) {
            val playOrder = env.playOrders[declaration.orderName]!!.map(::toPlayer)
            val prepopulated = declaration.moves.map { (pos, sym) ->
                (pos.col - 1 to pos.row - 1) to toPlayer(sym).token
            }.toMap()
            env.airspaceManager.add(declaration.name,
                env.airspaceManager.airspaceCreator.create(
                    *playOrder.toTypedArray(), prepopulated = prepopulated))
        }
    }
}
