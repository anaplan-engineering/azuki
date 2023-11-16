package com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.declaration

import com.anaplan.engineering.azuki.declaration.Declaration
import com.anaplan.engineering.azuki.declaration.DeclarationBuilder
import com.anaplan.engineering.azuki.declaration.FeDeclarationBuilderFactory
import com.anaplan.engineering.azuki.tictactoe.adapter.kazuki.EnvironmentBuilder

interface KazukiDeclarationBuilderFactory<D : Declaration> : FeDeclarationBuilderFactory<D, KazukiDeclarationBuilder<D>>

abstract class KazukiDeclarationBuilder<D : Declaration>(declaration: D) : DeclarationBuilder<D>(declaration) {

    fun declare(builder: EnvironmentBuilder) {
        build(builder)
    }

    abstract fun build(builder: EnvironmentBuilder)
}

