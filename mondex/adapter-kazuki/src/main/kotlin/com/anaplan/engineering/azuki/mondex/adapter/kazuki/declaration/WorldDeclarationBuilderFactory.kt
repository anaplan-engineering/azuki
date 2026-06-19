package com.anaplan.engineering.azuki.mondex.adapter.kazuki.declaration

import com.anaplan.engineering.azuki.mondex.adapter.declaration.declaration.WorldDeclaration
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.EnvironmentBuilder
import com.anaplan.engineering.azuki.mondex.adapter.kazuki.buildWorld

class WorldDeclarationBuilderFactory : KazukiDeclarationBuilderFactory<WorldDeclaration> {

    override val declarationClass = WorldDeclaration::class.java

    override fun create(declaration: WorldDeclaration): KazukiDeclarationBuilder<WorldDeclaration> =
        WorldDeclarationBuilder(declaration)

    private class WorldDeclarationBuilder(declaration: WorldDeclaration) :
        KazukiDeclarationBuilder<WorldDeclaration>(declaration) {

        override fun build(builder: EnvironmentBuilder) {
            builder.declare(declaration.name) {
                buildWorld(declaration.authPurses)
            }
        }
    }
}
