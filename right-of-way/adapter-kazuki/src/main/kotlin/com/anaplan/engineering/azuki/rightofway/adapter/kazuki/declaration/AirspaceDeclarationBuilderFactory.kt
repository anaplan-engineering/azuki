package com.anaplan.engineering.azuki.rightofway.adapter.kazuki.declaration

import com.anaplan.engineering.azuki.rightofway.adapter.declaration.declaration.AirspaceDeclaration
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.EnvironmentBuilder
import com.anaplan.engineering.azuki.rightofway.adapter.kazuki.toKazukiAirspace

class AirspaceDeclarationBuilderFactory : KazukiDeclarationBuilderFactory<AirspaceDeclaration> {

    override val declarationClass = AirspaceDeclaration::class.java
    override fun create(declaration: AirspaceDeclaration): KazukiDeclarationBuilder<AirspaceDeclaration> =
        AirspaceDeclarationBuilder(declaration)

    private class AirspaceDeclarationBuilder(declaration: AirspaceDeclaration) :
        KazukiDeclarationBuilder<AirspaceDeclaration>(declaration) {
        override fun build(builder: EnvironmentBuilder) {
            builder.declare(declaration.name) { env ->
                declaration.aircrafts.toKazukiAirspace()
            }
        }
    }
}
